package org.delia.type;

import java.util.HashMap;
import java.util.Map;

public class TypeReplaceSpec {
	public DType oldType;
	public DType newType;
	public int counter;
	private Map<DType,String> recursionProtectionMap = new HashMap<>();
	
	public boolean needsReplacement(DType dtype) {
		//handle the case of circular references
//		if (recursionProtectionMap.containsKey(dtype)) {
//			return false;
//		}
		
		boolean b = dtype == oldType;
		if (b) {
			dtype.invalidFlag = true;
			counter++;
			recursionProtectionMap.put(dtype, "");
		}
		return b;
	}
}
