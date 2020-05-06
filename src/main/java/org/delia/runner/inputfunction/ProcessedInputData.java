package org.delia.runner.inputfunction;

import java.util.HashMap;
import java.util.Map;

import org.delia.type.DStructType;

public class ProcessedInputData {
	public static class ProcessedValue {
		Object obj; //string usually
		boolean isVia;
		
		public ProcessedValue(Object obj, boolean isVia) {
			this.obj = obj;
			this.isVia = isVia;
		}
	}
	public DStructType structType;
	public Map<String,ProcessedValue> outputFieldMap = new HashMap<>(); //outfieldName,String
}