package org.delia.api;

import org.delia.Delia;
import org.delia.DeliaOptions;
import org.delia.DeliaSession;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.error.DeliaError;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.dat.DatService;
import org.delia.runner.DeliaRunner;
import org.delia.runner.ExecutionState;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigServiceImpl;
import org.delia.transaction.TransactionBody;
import org.delia.transaction.TransactionProvider;
import org.delia.transaction.VoidTransactionBody;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;

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
    public DeliaOptions sessionOptions; //private copy of delia options

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
        child.sessionOptions = delia.getOptions().clone();

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
        clone.varMap = cloneVarMap(execCtx.varMap);
        clone.currentSchema = execCtx.currentSchema;
        clone.enableRemoveFks = execCtx.enableRemoveFks;

        return clone;
    }

    //fix issue that child session was sharing objects with main session.
    //Need to do a deep copy
    private Map<String, ResultValue> cloneVarMap(Map<String, ResultValue> oldVarMap) {
//        return new ConcurrentHashMap<>(oldVarMap);
        Map<String, ResultValue> newVarMap = new ConcurrentHashMap<>();
        for(String key: oldVarMap.keySet()) {
            ResultValue res = oldVarMap.get(key);
            if (isNull(res)) {
                newVarMap.put(key, null);
                continue;
            }
            ResultValue newRes = new ResultValue();
            newRes.copyFrom(res);
            newVarMap.put(key, newRes);

            //that is still not good enough. res.val needs deep copying except for:
            //Integer, etc are immutable
            //DValue is immutable
            //but query response
            if (res.val instanceof QueryResponse) {
                QueryResponse qresp = (QueryResponse) res.val;
                QueryResponse copy = new QueryResponse();
                copy.ok = qresp.ok;
                copy.err = qresp.err;
                copy.dvalList = new ArrayList(qresp.dvalList);
                newRes.val = copy;
            }

        }
        return newVarMap;
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

    @Override
    public DeliaOptions getSessionOptions() {
        return sessionOptions;
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