package org.delia.varevaluator;

import org.delia.runner.ResultValue;
import org.delia.type.DValue;

import java.util.List;

public interface VarEvaluator {
	boolean exists(String varName);
	List<DValue> lookupVar(String varName);
	ResultValue lookupVarAsResultValue(String varName);
}