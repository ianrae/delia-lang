package org.delia.db.hls;

import org.delia.type.DStructType;

public class MTElement implements HLSElement {
	public DStructType structType;

	public MTElement(DStructType fromType) {
		this.structType = fromType;
	}

	public String getTypeName() {
		return structType.getName();
	}

	@Override
	public String toString() {
		String s = String.format("MT:%s", structType.getName());
		return s;
	}
}