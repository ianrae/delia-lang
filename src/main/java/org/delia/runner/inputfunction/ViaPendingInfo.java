package org.delia.runner.inputfunction;

import org.delia.type.DStructType;

public class ViaPendingInfo {
	public DStructType structType;
	public String outputFieldName;
	public Object processedInputValue;
//	private ProcessedInputData data;
	private LineObj lineObj;
	
	public ViaPendingInfo(DStructType structType, String outputFieldName, Object input, LineObj lineObj) {
		this.structType = structType;
		this.outputFieldName = outputFieldName;
		this.processedInputValue = input;
		this.lineObj = lineObj;
	}
}
