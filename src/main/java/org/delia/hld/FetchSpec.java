package org.delia.hld;

import org.delia.type.DStructType;

/**
 * Represents a fetch() or fks()
 * @author ian
 *
 */
public class FetchSpec {
	public DStructType structType;
	public String fieldName; 
	public boolean isFK; //if true then fks, else fetch
	
	public FetchSpec(DStructType structType, String fieldName) {
		this.structType = structType;
		this.fieldName = fieldName;
		this.isFK = false;
	}

	@Override
	public String toString() {
		String str = isFK ? ":fks" : "";
		String s = String.format("%s.%s%s", structType.toString(), fieldName, str);
		return s;
	}
}