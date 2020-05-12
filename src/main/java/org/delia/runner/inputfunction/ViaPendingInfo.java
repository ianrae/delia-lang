package org.delia.runner.inputfunction;

import org.delia.runner.inputfunction.ViaService.ViaInfo;
import org.delia.type.DStructType;

public class ViaPendingInfo {
	public DStructType structType;
	public String outputFieldName;
	public Object processedInputValue;
	public ViaInfo viaInfo;
	
	public ViaPendingInfo(DStructType structType, String outputFieldName, Object input, ViaInfo viaInfox) {
		this.structType = structType;
		this.outputFieldName = outputFieldName;
		this.processedInputValue = input;
		this.viaInfo = viaInfox;
	}
}
