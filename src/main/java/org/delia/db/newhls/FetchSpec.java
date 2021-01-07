package org.delia.db.newhls;

import org.delia.type.DStructType;

/**
 * Represents a fetch() or fks()
 * @author ian
 *
 */
public class FetchSpec {
	public DStructType structType;
	public String fieldName; //null if isFK
	public boolean isFK; //if true then fks, else fetch
	
	public FetchSpec(DStructType structType, String fieldName) {
		this.structType = structType;
		this.fieldName = fieldName;
		this.isFK = false;
	}
	public FetchSpec(DStructType structType) {
		this.structType = structType;
		this.fieldName = null;
		this.isFK = true;
	}

	@Override
	public String toString() {
		if (isFK) {
			return String.format("%s:fks", structType.toString());
		} else {
			String s = String.format("%s.%s", structType.toString(), fieldName);
			return s;
		}
	}
}