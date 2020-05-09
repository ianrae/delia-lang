package org.delia.db.hls;

import org.delia.type.TypePair;

public class FElement implements HLSElement {
	public TypePair fieldPair;

	public FElement(TypePair fieldPair) {
		this.fieldPair = fieldPair;
	}

	public String getFieldName() {
		return fieldPair.name;
	}
	@Override
	public String toString() {
		String s = String.format("F:%s", fieldPair.name);
		return s;
	}
	
}