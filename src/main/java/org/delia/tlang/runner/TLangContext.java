package org.delia.tlang.runner;

import java.util.Map;

import org.delia.runner.VarEvaluator;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;

public class TLangContext {
	public ScalarValueBuilder builder;
	public VarEvaluator varEvaluator;
	public Map<String, Object> inputDataMap;
	public boolean failFlag;
	public boolean stopFlag;
	public boolean stopAfterNextFlag;
	public int lineNum; //1-based
	
	public String getInputColumnAsString(String inputColumn) {
		Object obj = inputDataMap.get(inputColumn);
		if (obj == null) {
			return null;
		} else if (obj instanceof DValue) {
			DValue syntValue = (DValue) obj;
			return syntValue.asString();
		} else {
			return obj.toString(); //was string
		}
	}
}