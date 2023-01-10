package org.delia.api;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.dat.DatService;
import org.delia.runner.ExecutionState;
import org.delia.runner.ResultValue;
import org.delia.sprig.SprigServiceImpl;
import org.delia.transaction.TransactionBody;
import org.delia.transaction.TransactionProvider;
import org.delia.transaction.VoidTransactionBody;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;

import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DeliaSessionImpl implements DeliaSession {
    public boolean ok;
    public ExecutionState execCtx;
    public ResultValue res;

    //only set in beginExecution, not in continueExecution
    public AST.DeliaScript beginScript; //for getting at parse results. see DeliaOptions.saveParseExpObjectsInSession
    public AST.DeliaScript mostRecentContinueScript; //for getting at parse results. see DeliaOptions.saveParseExpObjectsInSession
    public DeliaExecutable mostRecentExecutable;
    //	private RunnerInitializer runnerInitializer;
    private Delia delia;
    public DatService datSvc; //	public DatIdMap datIdMap;
//	public Runner mostRecentRunner;
    public ZoneId zoneId;
//	public DBInterfaceFactory currentDBInterface;
	public TransactionProvider transactionProvider; //at most one

    public DeliaSessionImpl(Delia delia) {
        this.delia = delia;
    }

    @Override
    public boolean ok() {
        return ok;
    }

    @Override
    public ResultValue getFinalResult() {
        return res;
    }

    @Override
    public ExecutionState getExecutionContext() {
        return execCtx;
    }

    @Override
    public Delia getDelia() {
        return delia;
    }

    @Override
    public DatService getDatIdMap() {
        return datSvc;
    }

//	@Override
//	public void setRunnerIntiliazer(RunnerInitializer runnerInitializer) {
//		this.runnerInitializer = runnerInitializer;
//	}
//
//	@Override
//	public RunnerInitializer getRunnerIntiliazer() {
//		return runnerInitializer;
//	}
//
//	@Override
//	public DatIdMap getDatIdMap() {
//		return datIdMap;
//	}
//
//	@Override
//	public Runner getMostRecentRunner() {
//		return mostRecentRunner;
//	}

    @Override
    public DeliaSession createChildSession() {
        DeliaSessionImpl child = new DeliaSessionImpl(delia);
        child.ok = this.ok;
        child.execCtx = cloneExecCtx();
        child.res = null;
        child.beginScript = delia.getOptions().saveParseScriptInSession ? this.beginScript : null;
        child.mostRecentContinueScript = delia.getOptions().saveParseScriptInSession ? this.mostRecentContinueScript : null;
        child.datSvc = datSvc;
        child.zoneId = zoneId;

//		child.runnerInitializer = null;
//		child.datIdMap = this.datIdMap;
//		child.mostRecentRunner = null;
        //DAOs use child session so we want them to be in any current transaction
//		child.transactionProvider = transactionProvider;
        return child;
    }

    /**
     * The new session needs private generator, varMap, and sprigSvc.
     * The rest can be shared with the parent session.
     *
     * @return new execution state.
     */
    private ExecutionState cloneExecCtx() {
        ExecutionState clone = new ExecutionState();
//		clone.generator = mostRecentRunner == null ? null : mostRecentRunner.createGenerator();
//		clone.inputFnMap = execCtx.inputFnMap;
        clone.registry = execCtx.registry;
		clone.sprigSvc = new SprigServiceImpl(delia.getFactoryService(), execCtx.registry);
//		clone.userFnMap = execCtx.userFnMap;
        clone.varMap = new ConcurrentHashMap<>(execCtx.varMap);
        return clone;
    }

    @Override
    public ZoneId getDefaultTimezone() {
        return zoneId;
    }

    @Override
    public DTypeRegistry getRegistry() {
        return execCtx.registry;
    }

	@Override
	public <T> T runInTransaction(TransactionBody<T> body) {
		TransactionProvider transProvider = initTransProvider();
		T res = null;
		transProvider.beginTransaction();
		try {
			res = body.doSomething();
			transProvider.commitTransaction();
		} catch(Exception e) {
			transProvider.rollbackTransaction();
			endTransProvider();
			throw e;
		}
		endTransProvider();
		return res;
	}

	@Override
	public void runInTransactionVoid(VoidTransactionBody body) {
		TransactionProvider transProvider = initTransProvider();
		transProvider.beginTransaction();
		try {
			body.doSomething();
			transProvider.commitTransaction();
		} catch(Exception e) {
			transProvider.rollbackTransaction();
			endTransProvider();
			throw e;
		}
		endTransProvider();
	}


	private TransactionProvider initTransProvider() {
		if (transactionProvider != null) {
			DeliaExceptionHelper.throwError("nested-transactions-not-supported", "Not allowed to run a transaction within a transaction");
		}

		TransactionProvider transProvider = delia.getFactoryService().createTransactionProvider(delia.getDBInterface());
		this.transactionProvider = transProvider;
		return transProvider;
	}
	private void endTransProvider() {
		transactionProvider = null;
	}

}