package org.delia.type;

public class TypeReplaceSpec {
	public DType oldType;
	public DType newType;
	
	public boolean needsReplacement(DType dtype) {
		return dtype == oldType;
	}
}
