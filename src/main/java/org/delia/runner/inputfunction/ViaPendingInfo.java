package org.delia.runner.inputfunction;

import org.delia.runner.inputfunction.ViaService.ViaInfo;
import org.delia.type.DStructType;

public class ViaPendingInfo {
	public DStructType structType;
	public String outputFieldName;
	public Object processedInputValue;
	private ViaInfo viaInfo;
	
	public ViaPendingInfo(DStructType structType, String outputFieldName, Object input, ViaInfo viaInfo) {
		this.structType = structType;
		this.outputFieldName = outputFieldName;
		this.processedInputValue = input;
		this.viaInfo = viaInfo;
	}
}
