package org.delia.api;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.DeliaCompiler;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.db.DBErrorConverter;
import org.delia.db.schema.MigrationPlan;
import org.delia.db.schema.MigrationService;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.hld.HLDFacade;
import org.delia.hld.HLDFactory;
import org.delia.log.Log;
import org.delia.runner.BlobLoader;
import org.delia.runner.DeliaException;
import org.delia.runner.InternalCompileState;
import org.delia.runner.ResultValue;
import org.delia.runner.Runner;
import org.delia.runner.RunnerImpl;
import org.delia.runner.TypeRunner;
import org.delia.runner.TypeSpec;
import org.delia.type.DTypeRegistry;
import org.delia.typebuilder.FutureDeclError;
import org.delia.typebuilder.TypePreRunner;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zdb.DBInterfaceFactory;

public class DeliaImpl implements Delia {
	private static class MigrationExtraInfo {
		public DatIdMap datIdMap;
	}
	
//	public static boolean useNewHLD = true;
	
	private Log log;
	private DBInterfaceFactory dbInterface;
	private FactoryService factorySvc;
	private DeliaOptions deliaOptions = new DeliaOptions();
	private MigrationService migrationSvc;
	private ErrorAdjuster errorAdjuster;
	private HLDFactory hldFactory;
	
	public DeliaImpl(DBInterfaceFactory dbInterface, Log log, FactoryService factorySvc) {
		this.log = log;
		this.dbInterface = dbInterface;
		this.factorySvc = factorySvc;
		this.migrationSvc = new MigrationService(dbInterface, factorySvc);
		this.errorAdjuster = new ErrorAdjuster();
		this.hldFactory = dbInterface.getHLDFactory();
	}

	@Override
	public ResultValue execute(String src, BlobLoader blobLoader) {
		List<Exp> expL = compileDeliaSource(src, true);

		Runner runner = createRunner(null, blobLoader);
		execTypes(runner, expL);
		MigrationExtraInfo extraInfo = new MigrationExtraInfo();
		ResultValue migrationPlanRes = doPass3AndDBMigration(src, expL, runner, null, extraInfo);
		if (migrationPlanRes != null) {
			return migrationPlanRes;
		}

		return doExecute(runner, expL, extraInfo.datIdMap);
	}


	private ResultValue doExecute(Runner runner, List<Exp> expL, DatIdMap datIdMap) {
		ResultValue res = null;
		if (!deliaOptions.enableExecution) {
			res = new ResultValue();
			res.ok = true;
			return res;
		}

		runner.setDatIdMap(datIdMap);
		res = runner.executeProgram(expL);
		if (res != null && ! res.ok) {
			List<DeliaError> errL = errorAdjuster.adjustErrors(res.errors);
			throw new DeliaException(errL);
		}
		return res;
	}


	//only public for unit tests
	public Runner createRunner(DeliaSession dbsess, BlobLoader blobLoader) {
		ErrorTracker et = new SimpleErrorTracker(log);
		Runner runner = new RunnerImpl(factorySvc, dbInterface, hldFactory, blobLoader);
		RunnerInitializer runnerInitializer = dbsess == null ? null: dbsess.getRunnerIntiliazer();
		if (runnerInitializer != null) {
			runnerInitializer.initialize(runner);
		}
		boolean b; 
		if (dbsess == null) {
			b = runner.init(null); 
		} else {
			b = runner.init(dbsess.getExecutionContext());
		}

		if (! b) {
			DeliaError err = et.add("runner-init-failed", "runner init failed");
			throw new DeliaException(err);
		}
		
		HLDFacade hldFacade = new HLDFacade(this, runner.getRegistry(), runner);
		runner.setHLDFacade(hldFacade);

		if (deliaOptions.dbObserverFactory != null) {
			dbInterface.setObserverFactory(deliaOptions.dbObserverFactory);
			dbInterface.setIgnoreSimpleSvcSql(deliaOptions.observeHLDSQLOnly);
		}
		
		return runner;
	}

	@Override
	public DeliaSession beginSession(String src) {
		return doBeginExecution(src, null, null);
	}
	private DeliaSession doBeginExecution(String src, BlobLoader blobLoader, MigrationPlan plan) {
		List<Exp> expL =  compileDeliaSource(src, false);

		//1st pass
		Runner mainRunner = createRunner(null, blobLoader);
		execTypes(mainRunner, expL);
		MigrationExtraInfo extraInfo = new MigrationExtraInfo();
		ResultValue migrationPlanRes = doPass3AndDBMigration(src, expL, mainRunner, plan, extraInfo);
		if (migrationPlanRes != null) {
			DeliaSessionImpl session = new DeliaSessionImpl(this);
			session.execCtx = mainRunner.getExecutionState();
			session.ok = true;
			session.res = migrationPlanRes;
			session.expL = deliaOptions.saveParseExpObjectsInSession ? expL : null;
			session.datIdMap = extraInfo.datIdMap;
			session.mostRecentRunner = mainRunner;
			session.zoneId = factorySvc.getTimeZoneService().getDefaultTimeZone();
			return session;
		}

		ResultValue res = doExecute(mainRunner, expL, extraInfo.datIdMap);

		DeliaSessionImpl session = new DeliaSessionImpl(this);
		session.execCtx = mainRunner.getExecutionState();
		session.ok = true;
		session.res = res;
		session.expL = deliaOptions.saveParseExpObjectsInSession ? expL : null;
		session.datIdMap = extraInfo.datIdMap;
		session.mostRecentRunner = mainRunner;
		session.zoneId = factorySvc.getTimeZoneService().getDefaultTimeZone();
		return session;
	}

	@Override
	public DeliaSession executeMigrationPlan(String src, MigrationPlan plan) {
		return doBeginExecution(src, null, plan);
	}

	protected void execTypes(Runner mainRunner, List<Exp> extL) {
		List<DeliaError> allErrors = new ArrayList<>();

		//1st pass
		DTypeRegistry registry = mainRunner.getRegistry();
		TypePreRunner preRunner = new TypePreRunner(factorySvc, registry);
		preRunner.executeStatements(extL, allErrors);
		if (!allErrors.isEmpty()) {
			//something went wrong
			throw new DeliaException(allErrors);
		}
		
		//2nd pass
		TypeRunner typeRunner = mainRunner.createTypeRunner();
		typeRunner.setPreRegistry(preRunner.getPreRegistry());
		typeRunner.executeStatements(extL, allErrors, true);
		
		if (!allErrors.isEmpty()) {
			//something went wrong
			throw new DeliaException(allErrors);
		}
		
		//replace error converter with a registry aware one (better at parsing errors)
		DBErrorConverter errorConverter = dbInterface.getDBErrorConverter();
		if (errorConverter != null) {
//		RegistryAwareDBErrorConverter radbec = new RegistryAwareDBErrorConverter(errorConverter, registry);
//		dbInterface.setDBErrorConverter(radbec);
			errorConverter.setRegistry(registry);
		}
	}

	private ResultValue doPass3AndDBMigration(String src, List<Exp> extL, Runner mainRunner, MigrationPlan plan, MigrationExtraInfo extraInfo) {
		InternalCompileState execCtx = mainRunner.getCompileState();
		for(Exp exp: extL) {
			if (exp instanceof TypeStatementExp) {
				TypeStatementExp typeExp = (TypeStatementExp) exp;
				TypeSpec tt = execCtx.compiledTypeMap.get(typeExp.typeName);
				if (tt != null) {
					tt.typeExp = typeExp;
				}
			}
		}

		CompilerHelper compilerHelper = createCompilerHelper();
		compilerHelper.executePass3and4(execCtx, src, extL, mainRunner.getRegistry());
		
		//load or assign DAT ids. must do this even if don't do migration
		extraInfo.datIdMap = migrationSvc.loadDATData(mainRunner.getRegistry(), mainRunner);
		DatIdMap datIdMap = extraInfo.datIdMap;
		
		//now that we know the types, do a flyway-style schema migration
		//if the db supports it.
		if (dbInterface.getCapabilities().requiresSchemaMigration()) {
			migrationSvc.initPolicy(deliaOptions.useSafeMigrationPolicy, deliaOptions.enableAutomaticMigrations);
			
			switch(deliaOptions.migrationAction) {
			case MIGRATE:
			{
				boolean b;
				if (deliaOptions.disableSQLLoggingDuringSchemaMigration) {
					boolean prev = dbInterface.isSQLLoggingEnabled();
					dbInterface.enableSQLLogging(false);
					b = migrationSvc.autoMigrateDbIfNeeded(mainRunner.getRegistry(), mainRunner, datIdMap);
					dbInterface.enableSQLLogging(prev);
				} else {
					b = migrationSvc.autoMigrateDbIfNeeded(mainRunner.getRegistry(), mainRunner, datIdMap);
				}
				
				if (!b) {
					DeliaExceptionHelper.throwError("migration-failed-due-to-policy", "migration needed but not run - can't start");
				}
			}
			break;
			case GENERATE_MIGRATION_PLAN:
			{
				ResultValue res = new ResultValue();
				res.ok = true;
				res.val = migrationSvc.createMigrationPlan(mainRunner.getRegistry(), mainRunner, datIdMap);
				return res;
			}
			case RUN_MIGRATION_PLAN:
			{
				ResultValue res = new ResultValue();
				res.ok = true;
				res.val = migrationSvc.runMigrationPlan(mainRunner.getRegistry(), plan, mainRunner, datIdMap);
				return res;
			}
			case DO_NOTHING:
			default:
				break;
			}
		}
		return null;
	}


	public int numFutureDeclErrors(List<DeliaError> errL) {
		int count = 0;
		for(DeliaError err: errL) {
			if (err instanceof FutureDeclError) {
				count++;
			}
		}
		return count;
	}
	public List<DeliaError> getNonFutureDeclErrors(List<DeliaError> errL) {
		List<DeliaError> filteredL = new ArrayList<>();
		for(DeliaError err: errL) {
			if (err instanceof FutureDeclError) {
			} else {
				filteredL.add(err);
			}
		}
		return filteredL;
	}

	@Override
	public ResultValue continueExecution(String src, BlobLoader blobLoader, DeliaSession session) {
		Runner mostRecentRunner = session.getMostRecentRunner();
		InternalCompileState execCtx = mostRecentRunner == null ? null : mostRecentRunner.getCompileState();
		if (execCtx != null) {
			execCtx.delcaredVarMap.remove(RunnerImpl.DOLLAR_DOLLAR);
			execCtx.delcaredVarMap.remove(RunnerImpl.VAR_SERIAL);
		}

		CompilerHelper compilerHelper = createCompilerHelper();
		List<Exp> expL = compilerHelper.compileDeliaSource(src, execCtx);
		for(Exp exp: expL) {
			if (exp instanceof TypeStatementExp) {
				String msg = String.format("'type' statements not allowed in continueExecution - %s", exp.strValue());
				DeliaError err = new DeliaError("type-statement-not-allowed", msg);
				throw new DeliaException(err);
			}
		}

		Runner runner = createRunner(session, blobLoader);
		ResultValue res = doExecute(runner, expL, session.getDatIdMap());
		
		if (session instanceof DeliaSessionImpl) {
			DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
			sessimpl.mostRecentContinueExpL = deliaOptions.saveParseExpObjectsInSession ? expL : null;
			sessimpl.mostRecentRunner = runner;
			sessimpl.res = res;
		}
		return res;
	}
	
	public List<Exp>  continueCompile(String src, DeliaSession session) {
		Runner mostRecentRunner = session.getMostRecentRunner();
		InternalCompileState execCtx = mostRecentRunner == null ? null : mostRecentRunner.getCompileState();
		if (execCtx != null) {
			execCtx.delcaredVarMap.remove(RunnerImpl.DOLLAR_DOLLAR);
			execCtx.delcaredVarMap.remove(RunnerImpl.VAR_SERIAL);
		}

		CompilerHelper compilerHelper = createCompilerHelper();
		List<Exp> expL = compilerHelper.compileDeliaSource(src, execCtx);
		for(Exp exp: expL) {
			if (exp instanceof TypeStatementExp) {
				String msg = String.format("'type' statements not allowed in continueExecution - %s", exp.strValue());
				DeliaError err = new DeliaError("type-statement-not-allowed", msg);
				throw new DeliaException(err);
			}
		}
		return expL;
	}
	
	@Override
	public DeliaCompiler createCompiler()  {
		return doCreateCompiler(null);
	}
	private DeliaCompiler doCreateCompiler(InternalCompileState execCtx)  {
		CompilerHelper helper = createCompilerHelper();
		DeliaCompiler compiler = helper.createCompiler(execCtx);
		return compiler;
	}
	
	private List<Exp> compileDeliaSource(String src, boolean doPass3Flag) {
		CompilerHelper helper = createCompilerHelper();
		List<Exp> expL = helper.compileDeliaSource(src, doPass3Flag);
		return expL;
	}
	private CompilerHelper createCompilerHelper() {
		return new CompilerHelper(dbInterface, log, factorySvc, deliaOptions);
	}
	
	

	@Override
	public Log getLog() {
		return log;
	}

	@Override
	public FactoryService getFactoryService() {
		return factorySvc;
	}

	@Override
	public DeliaOptions getOptions() {
		return deliaOptions;
	}

	@Override
	public DBInterfaceFactory getDBInterface() {
		return dbInterface;
	}
	//for internal use only - unit tests
	public void setDbInterface(DBInterfaceFactory dbInterface) {
		this.dbInterface = dbInterface;
	}
	
	private String readAllText(BufferedReader reader) {
		 return reader.lines()
			      .collect(Collectors.joining(System.lineSeparator()));
	}

	@Override
	public ResultValue execute(BufferedReader reader) {
		String src = readAllText(reader);
		return this.execute(src);
	}

	@Override
	public DeliaSession beginSession(BufferedReader reader) {
		String src = readAllText(reader);
		return this.beginSession(src);
	}

	@Override
	public ResultValue continueExecution(BufferedReader reader, DeliaSession dbsess) {
		String src = readAllText(reader);
		return this.continueExecution(src, dbsess);
	}

	@Override
	public DeliaSession executeMigrationPlan(BufferedReader reader, MigrationPlan plan) {
		String src = readAllText(reader);
		return this.executeMigrationPlan(reader, plan);
	}

	@Override
	public HLDFactory getHLDFactory() {
		return hldFactory;
	}

	@Override
	public DeliaSession beginSession(String src, BlobLoader blobLoader) {
		return beginSession(src, null);
	}

	@Override
	public DeliaSession executeMigrationPlan(String src, BlobLoader blobLoader, MigrationPlan plan) {
		return doBeginExecution(src, blobLoader, plan);
	}

	@Override
	public ResultValue execute(BufferedReader reader, BlobLoader blobLoader) {
		String src = readAllText(reader);
		return execute(src, blobLoader);
	}

	@Override
	public DeliaSession beginSession(BufferedReader reader, BlobLoader blobLoader) {
		String src = readAllText(reader);
		return beginSession(src, blobLoader);
	}

	@Override
	public DeliaSession executeMigrationPlan(BufferedReader reader, BlobLoader blobLoader, MigrationPlan plan) {
		String src = readAllText(reader);
		return executeMigrationPlan(src, blobLoader, plan);
	}

	@Override
	public ResultValue execute(String src) {
		return execute(src, null);
	}

	@Override
	public ResultValue continueExecution(String src, DeliaSession dbsess) {
		return continueExecution(src, null, dbsess);
	}

	@Override
	public ResultValue continueExecution(BufferedReader reader, BlobLoader blobLoader, DeliaSession dbsess) {
		String src = readAllText(reader);
		return continueExecution(src, blobLoader, dbsess);
	}
}