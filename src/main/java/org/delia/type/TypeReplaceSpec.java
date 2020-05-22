package org.delia.type;

import java.util.HashMap;
import java.util.Map;

public class TypeReplaceSpec {
	public static final int MAX_FIELDS_PER_STRUCT = 1000;
	
	
	public DType oldType;
	public DType newType;
	public int counter;
	private Map<Object,Integer> recursionProtectionMap = new HashMap<>();
	
	public boolean needsReplacement(Object visitor, DType dtype) {
		//handle the case of circular references.
		//careful. the same visitor can be called more than once if has multiple fields.
		if (recursionProtectionMap.containsKey(visitor)) {
			Integer n = recursionProtectionMap.get(visitor);
			if (n < MAX_FIELDS_PER_STRUCT) {
				recursionProtectionMap.put(visitor, n+1);
			} else {
				return false;
			}
		}
		
		boolean b = dtype == oldType;
		if (b) {
			dtype.invalidFlag = true;
			counter++;
			recursionProtectionMap.put(visitor, 1);
		}
		return b;
	}
}
