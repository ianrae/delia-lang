package org.delia.runner.inputfunction;

import org.delia.type.DStructType;

public class ViaPendingInfo {
	public DStructType structType;
	public String outputFieldName;
	public Object processedInputValue;
	
	public ViaPendingInfo(DStructType structType, String outputFieldName, Object input) {
		this.structType = structType;
		this.outputFieldName = outputFieldName;
		this.processedInputValue = input;
	}
}
