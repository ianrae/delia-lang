package org.delia.api;

import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.runner.ExecutionState;
import org.delia.runner.ResultValue;

public class DeliaSessionImpl implements DeliaSession {
	public boolean ok;
	public ExecutionState execCtx;
	public ResultValue res;
	
	//only set in beginExecution, not in continueExecution
	public List<Exp> expL; //for getting at parse results. TODO: need a flag that disables this to save memory
	public List<Exp> mostRecentContinueExpL; //for getting at parse results. TODO: need a flag that disables this to save memory
//	private DValueIterator insertPrebuiltValueIterator;
	private RunnerInitializer runnerInitializer;
	private Delia delia;
						
	
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
	
}