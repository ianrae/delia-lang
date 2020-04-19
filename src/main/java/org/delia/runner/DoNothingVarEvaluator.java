package org.delia.runner;

import java.util.List;

import org.delia.type.DValue;

public class DoNothingVarEvaluator implements VarEvaluator {

	@Override
	public List<DValue> lookupVar(String varName) {
		return null;
	}

	@Override
	public String evalVarAsString(String varName, String typeName) {
		return null;
	}
}
