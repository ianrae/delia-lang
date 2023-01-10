package org.delia.varevaluator;

import org.delia.runner.ResultValue;
import org.delia.type.DValue;

import java.util.List;

public class DoNothingVarEvaluator implements VarEvaluator {

	@Override
	public boolean exists(String varName) {
		return true; //fake it
	}
	@Override
	public List<DValue> lookupVar(String varName) {
		return null;
	}

	@Override
	public ResultValue lookupVarAsResultValue(String varName) {
		return null;
	}

}
