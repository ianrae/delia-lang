package org.delia.api;

import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.runner.DValueIterator;
import org.delia.runner.ExecutionState;
import org.delia.runner.ResultValue;

public class DeliaSessionImpl implements DeliaSession {
	public boolean ok;
	public ExecutionState execCtx;
	public ResultValue res;
	
	//only set in beginExecution, not in continueExecution
	public List<Exp> expL; //for getting at parse results. TODO: need a flag that disables this to save memory
	private DValueIterator insertPrebuiltValueIterator;
						

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
	public void setInsertPrebuiltValueIterator(DValueIterator insertPrebuiltValueIterator) {
		this.insertPrebuiltValueIterator = insertPrebuiltValueIterator;
	}

	@Override
	public DValueIterator getInsertPrebuiltValueIterator() {
		return insertPrebuiltValueIterator;
	}
	
}