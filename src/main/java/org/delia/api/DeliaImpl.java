package org.delia.api;

import java.util.ArrayList;
import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.DeliaCompiler;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.db.DBInterface;
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
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypeReplaceSpec;
import org.delia.typebuilder.FutureDeclError;
import org.delia.util.DeliaExceptionHelper;

public class DeliaImpl implements Delia {
	private static class MigrationExtraInfo {
		public DatIdMap datIdMap;
	}
	
	private Log log;
	private DBInterface dbInterface;
	private FactoryService factorySvc;
	private DeliaOptions deliaOptions = new DeliaOptions();
	private MigrationService migrationSvc;
	private Runner mostRecentRunner;

	public DeliaImpl(DBInterface dbInterface, Log log, FactoryService factorySvc) {
		this.log = log;
		this.dbInterface = dbInterface;
		this.factorySvc = factorySvc;
		this.migrationSvc = new MigrationService(dbInterface, factorySvc);
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

		return doExecute(runner, expL);
	}

	private ResultValue doExecute(Runner runner, List<Exp> expL) {
		ResultValue res = null;
		if (!deliaOptions.enableExecution) {
			res = new ResultValue();
			res.ok = true;
			return res;
		}

		res = runner.executeProgram(expL);
		if (res != null && ! res.ok) {
			throw new DeliaException(res.errors);
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


	protected Runner createRunner(DeliaSession dbsess) {
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
		
		HLSManager mgr = new HLSManager(this, runner.getRegistry(), dbsess, runner);
		if (deliaOptions.useHLS) {
			runner.setHLSManager(mgr);
		}
		
		dbInterface.init(factorySvc);
		mostRecentRunner = runner;
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
			session.expL = expL;
			session.datIdMap = extraInfo.datIdMap;
			return session;
		}

		ResultValue res = doExecute(mainRunner, expL);

		DeliaSessionImpl session = new DeliaSessionImpl(this);
		session.execCtx = mainRunner.getExecutionState();
		session.ok = true;
		session.res = res;
		session.expL = expL;
		session.datIdMap = extraInfo.datIdMap;
		return session;
	}

	@Override
	public DeliaSession executeMigrationPlan(String src, MigrationPlan plan) {
		return doBeginExecution(src, plan);
	}

	protected void execTypes(Runner mainRunner, List<Exp> extL) {
		List<DeliaError> allErrors = new ArrayList<>();

		//1st pass
		TypeRunner typeRunner = mainRunner.createTypeRunner();
		typeRunner.executeStatements(extL, allErrors, true);
		
		if (allErrors.isEmpty()) {
			return;
		} else if (numFutureDeclErrors(allErrors) != allErrors.size()) {
			//something else went wrong
			throw new DeliaException(getNonFutureDeclErrors(allErrors));
		}
		log.log("%d forward decls found - re-executing.", numFutureDeclErrors(allErrors));
		
		//2nd pass - for future decls
		allErrors.clear();
		List<Exp> newExtL = typeRunner.getNeedReexecuteL(); //only re-exec the failed types
		DTypeRegistry registry = typeRunner.getRegistry();
		List<DType> oldTypeL = new ArrayList<>();
		for(Exp exp: newExtL) {
			TypeStatementExp texp = (TypeStatementExp) exp;
			DType dtype = registry.getType(texp.typeName);
			oldTypeL.add(dtype);
		}
		
		//Type Replacement - because of the after-you-after-you problem with relations, 
		//newExtL will contain types that could fully be built because of forward declarations
		//Our solution is to run the typerunner again on just the broken types, and then
		//use a visitor pattern to get all parts of the registry, types, rules, etc to update
		//themselves with the new (correct) version of the type.
		
		//prepare the type replacer
		List<TypeReplaceSpec> replacerL = new ArrayList<>();
		for(DType oldtype: oldTypeL) {
			TypeReplaceSpec spec = new TypeReplaceSpec();
			spec.oldType = oldtype;
			replacerL.add(spec);
		}
		
		typeRunner.executeStatements(newExtL, allErrors, false);
		
		//now update all types
		for(TypeReplaceSpec spec: replacerL) {
			spec.newType = registry.getType(spec.oldType.getName());
			registry.performTypeReplacement(spec);
			log.log("type-replacement '%s' %d", spec.newType.getName(), spec.counter);
			
			if (dbInterface.getCapabilities().isRequiresTypeReplacementProcessing()) {
				dbInterface.performTypeReplacement(spec);
			}
		}
		
		//and check that we did all replacement
		for(String typeName: typeRunner.getRegistry().getAll()) {
			DType dtype = registry.getType(typeName);
			if (dtype.invalidFlag) {
				log.logError("ERROR1: type %s invalid", dtype.getName());
			}
		}
		
		typeRunner.executeRulePostProcessor(allErrors);
		
		//TODO: are 2 passes enough?
		if (allErrors.isEmpty()) {
			return;
		} else {
			//something else went wrong
			throw new DeliaException(allErrors);
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

		DeliaCompiler compiler = doCreateCompiler(execCtx);
		compiler.executePass3(src, extL);

		//and do pass4
		compiler.executePass4(src, extL, mainRunner.getRegistry());
		
		//load or assign DAT ids. must do this even if don't do migration
		extraInfo.datIdMap = migrationSvc.loadDATData(mainRunner.getRegistry(), mainRunner);
		
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
					b = migrationSvc.autoMigrateDbIfNeeded(mainRunner.getRegistry(), mainRunner);
					dbInterface.enableSQLLogging(prev);
				} else {
					b = migrationSvc.autoMigrateDbIfNeeded(mainRunner.getRegistry(), mainRunner);
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
				res.val = migrationSvc.createMigrationPlan(mainRunner.getRegistry(), mainRunner);
				return res;
			}
			case RUN_MIGRATION_PLAN:
			{
				ResultValue res = new ResultValue();
				res.ok = true;
				res.val = migrationSvc.runMigrationPlan(mainRunner.getRegistry(), plan, mainRunner);
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
		ResultValue res = doExecute(runner, expL);
		
		if (session instanceof DeliaSessionImpl) {
			DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
			sessimpl.mostRecentContinueExpL = expL;
		}
		return res;
	}
	
	public List<Exp>  continueCompile(String src, DeliaSession session) {
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
	public DBInterface getDBInterface() {
		return dbInterface;
	}
	//for internal use only - unit tests
	public void setDbInterface(DBInterface dbInterface) {
		this.dbInterface = dbInterface;
	}

	public Runner getMostRecentRunner() {
		return mostRecentRunner;
	}
}