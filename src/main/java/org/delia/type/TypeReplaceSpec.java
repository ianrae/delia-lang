package org.delia.type;

import java.util.HashMap;
import java.util.Map;

public class TypeReplaceSpec {
	public DType oldType;
	public DType newType;
	public int counter;
	private Map<Object,String> recursionProtectionMap = new HashMap<>();
	
	public boolean needsReplacement(Object visitor, DType dtype) {
		//handle the case of circular references
		if (recursionProtectionMap.containsKey(visitor)) {
			return false;
		}
		
		boolean b = dtype == oldType;
		if (b) {
			dtype.invalidFlag = true;
			counter++;
			recursionProtectionMap.put(visitor, "");
		}
		return b;
	}
}
