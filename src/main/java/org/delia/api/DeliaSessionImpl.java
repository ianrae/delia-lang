package org.delia.api;

import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.Exp;
import org.delia.runner.ExecutionState;
import org.delia.runner.ResultValue;
import org.delia.runner.Runner;
import org.delia.sprig.SprigServiceImpl;
import org.delia.type.DTypeRegistry;

public class DeliaSessionImpl implements DeliaSession {
	public boolean ok;
	public ExecutionState execCtx;
	public ResultValue res;
	
	//only set in beginExecution, not in continueExecution
	public List<Exp> expL; //for getting at parse results. see DeliaOptions.saveParseExpObjectsInSession
	public List<Exp> mostRecentContinueExpL; //for getting at parse results. see DeliaOptions.saveParseExpObjectsInSession
	private RunnerInitializer runnerInitializer;
	private Delia delia;
	public DatIdMap datIdMap;
	public Runner mostRecentRunner;
	public ZoneId zoneId;
	
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
	public void setRunnerIntiliazer(RunnerInitializer runnerInitializer) {
		this.runnerInitializer = runnerInitializer;
	}

	@Override
	public RunnerInitializer getRunnerIntiliazer() {
		return runnerInitializer;
	}

	@Override
	public DatIdMap getDatIdMap() {
		return datIdMap;
	}

	@Override
	public Runner getMostRecentRunner() {
		return mostRecentRunner;
	}

	@Override
	public DeliaSession createChildSession() {
		DeliaSessionImpl child = new DeliaSessionImpl(delia);
		child.ok = this.ok;
		child.execCtx = cloneExecCtx();
		child.res = null;
		child.expL = delia.getOptions().saveParseExpObjectsInSession ? this.expL : null; 
		child.mostRecentContinueExpL = delia.getOptions().saveParseExpObjectsInSession ? this.mostRecentContinueExpL : null; 
		child.runnerInitializer = null;
		child.datIdMap = this.datIdMap;
		child.mostRecentRunner = null;
		child.zoneId = zoneId;
		return child;
	}

	/**
	 * The new session needs private generator, varMap, and sprigSvc.
	 * The rest can be shared with the parent session.
	 * @return new execution state.
	 */
	private ExecutionState cloneExecCtx() {
		ExecutionState clone = new ExecutionState();
		clone.generator = mostRecentRunner == null ? null : mostRecentRunner.createGenerator();
		clone.inputFnMap = execCtx.inputFnMap;
		clone.registry = execCtx.registry;
		clone.sprigSvc = new SprigServiceImpl(delia.getFactoryService(), execCtx.registry);
		clone.userFnMap = execCtx.userFnMap;
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
	
}