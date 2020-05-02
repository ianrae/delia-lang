package org.delia.tlang.runner;

import java.util.Map;

import org.delia.runner.VarEvaluator;
import org.delia.type.DValue;

public interface TLangRunner {
	void setVarEvaluator(VarEvaluator varEvaluator);
	TLangResult execute(TLangProgram program, DValue initialValue);
	TLangResult executeOne(TLangStatement statement, DValue initialValue);
	String getTrail();
	void setInputMap(Map<String, Object> inputData);
}