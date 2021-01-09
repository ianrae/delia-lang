package org.delia.tlang.runner;

import java.util.Collections;
import java.util.List;

import org.delia.runner.ExecutionState;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.runner.VarEvaluator;
import org.delia.type.DValue;

public class TLangVarEvaluator implements VarEvaluator {

	private ExecutionState execState;
	private DValue valueVar;

	public TLangVarEvaluator(ExecutionState execState) {
		this.execState = execState;
	}

	@Override
	public List<DValue> lookupVar(String varName) {
		if (varName.equals("value")) {
			return Collections.singletonList(valueVar);
		}
		
		ResultValue res = execState.varMap.get(varName);
		if (res.val instanceof DValue) {
			DValue dval = (DValue) res.val;
			return Collections.singletonList(dval);
		}
		List<DValue> list = res.getAsDValueList();
		return list;
	}

	@Override
	public String evalVarAsString(String varName, String typeName) {
		throw new IllegalArgumentException("SprigVarEvaluator.lookupVar not IMPLEMENTED!");
	}

	public void setValueVar(DValue initialValue) {
		this.valueVar = initialValue;
	}
	
	public void setLineNum(DValue nval) {
		QueryResponse qresp = new QueryResponse();
		qresp.ok = true;
		qresp.dvalList = Collections.singletonList(nval);
		ResultValue res = new ResultValue();
		res.ok = true;
		res.val = qresp;
		execState.varMap.put("LINENUM", res);
	}
}
