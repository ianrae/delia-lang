package org.delia.tlang.runner;

import java.util.Map;

import org.delia.runner.VarEvaluator;
import org.delia.valuebuilder.ScalarValueBuilder;

public class TLangContext {
	public ScalarValueBuilder builder;
	public VarEvaluator varEvaluator;
	public Map<String, String> inputDataMap;
	public boolean failFlag;
	public boolean stopFlag;
	public boolean stopAfterNextFlag;
}