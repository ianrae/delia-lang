package org.delia.api;

import java.util.ArrayList;
import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.DeliaCompiler;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.db.DBErrorConverter;
import org.delia.db.RegistryAwareDBErrorConverter;
import org.delia.db.hld.HLDManager;
import org.delia.db.hls.manager.HLSManager;
import org.delia.db.schema.MigrationPlan;
import org.delia.db.schema.MigrationService;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
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
import org.delia.zdb.ZDBInterfaceFactory;

public class DeliaImpl implements Delia {
	private static class MigrationExtraInfo {
		public DatIdMap datIdMap;
	}
	
	public static boolean useNewHLD = true;
	
	private Log log;
	private ZDBInterfaceFactory dbInterface;
	private FactoryService factorySvc;
	private DeliaOptions deliaOptions = new DeliaOptions();
	private MigrationService migrationSvc;
	private ErrorAdjuster errorAdjuster;
	
	public DeliaImpl(ZDBInterfaceFactory dbInterface, Log log, FactoryService factorySvc) {
		this.log = log;
		this.dbInterface = dbInterface;
		this.factorySvc = factorySvc;
		this.migrationSvc = new MigrationService(dbInterface, factorySvc);
		this.errorAdjuster = new ErrorAdjuster();
	}

	@Override
	public ResultValue execute(String src) {
		DeliaCompiler compiler = createCompiler();
		List<Exp> expL = compiler.parse(src);

		Runner runner = createRunner(null);
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

	@Override
	public DeliaCompiler createCompiler()  {
		return doCreateCompiler(null);
	}
	private DeliaCompiler doCreateCompiler(InternalCompileState execCtx)  {
		DeliaCompiler compiler = new DeliaCompiler(factorySvc, execCtx);
		return compiler;
	}


	//only public for unit tests
	public Runner createRunner(DeliaSession dbsess) {
		ErrorTracker et = new SimpleErrorTracker(log);
		Runner runner = new RunnerImpl(factorySvc, dbInterface);
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
		
		if (useNewHLD) {
			HLDManager mgr = new HLDManager(this, runner.getRegistry(), runner);
			runner.setHLDManager(mgr);
		} else {
			HLSManager mgr = new HLSManager(this, runner.getRegistry(), dbsess, runner);
			runner.setHLSManager(mgr);
		}
		return runner;
	}

	@Override
	public DeliaSession beginSession(String src) {
		return doBeginExecution(src, null);
	}
	private DeliaSession doBeginExecution(String src, MigrationPlan plan) {
		DeliaCompiler compiler = createCompiler();
		compiler.setDoPass3Flag(false);
		List<Exp> expL = compiler.parse(src);

		//1st pass
		Runner mainRunner = createRunner(null);
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
		return doBeginExecution(src, plan);
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
		RegistryAwareDBErrorConverter radbec = new RegistryAwareDBErrorConverter(errorConverter, registry);
		dbInterface.setDBErrorConverter(radbec);
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

		DeliaCompiler compiler = doCreateCompiler(execCtx);
		compiler.executePass3(src, extL);

		//and do pass4
		compiler.executePass4(src, extL, mainRunner.getRegistry());
		
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
	public ResultValue continueExecution(String src, DeliaSession session) {
		Runner mostRecentRunner = session.getMostRecentRunner();
		InternalCompileState execCtx = mostRecentRunner == null ? null : mostRecentRunner.getCompileState();
		if (execCtx != null) {
			execCtx.delcaredVarMap.remove(RunnerImpl.DOLLAR_DOLLAR);
			execCtx.delcaredVarMap.remove(RunnerImpl.VAR_SERIAL);
		}

		DeliaCompiler compiler = doCreateCompiler(execCtx);
		List<Exp> expL = compiler.parse(src);
		for(Exp exp: expL) {
			if (exp instanceof TypeStatementExp) {
				String msg = String.format("'type' statements not allowed in continueExecution - %s", exp.strValue());
				DeliaError err = new DeliaError("type-statement-not-allowed", msg);
				throw new DeliaException(err);
			}
		}

		Runner runner = createRunner(session);
		ResultValue res = doExecute(runner, expL, session.getDatIdMap());
		
		if (session instanceof DeliaSessionImpl) {
			DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
			sessimpl.mostRecentContinueExpL = deliaOptions.saveParseExpObjectsInSession ? expL : null;
			sessimpl.mostRecentRunner = runner;
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

		DeliaCompiler compiler = doCreateCompiler(execCtx);
		List<Exp> expL = compiler.parse(src);
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
	public ZDBInterfaceFactory getDBInterface() {
		return dbInterface;
	}
	//for internal use only - unit tests
	public void setDbInterface(ZDBInterfaceFactory dbInterface) {
		this.dbInterface = dbInterface;
	}
}