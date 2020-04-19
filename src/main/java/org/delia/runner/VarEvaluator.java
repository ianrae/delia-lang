package org.delia.runner;

import java.util.List;

import org.delia.type.DValue;

public interface VarEvaluator {
	List<DValue> lookupVar(String varName);
	String evalVarAsString(String varName, String typeName);
}