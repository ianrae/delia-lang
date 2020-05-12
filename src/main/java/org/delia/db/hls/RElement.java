package org.delia.db.hls;

import org.delia.type.TypePair;

public class RElement implements HLSElement {
	public TypePair rfieldPair;

	public RElement(TypePair rfieldPair) {
		this.rfieldPair = rfieldPair;
	}

	@Override
	public String toString() {
		String s = String.format("R:%s", rfieldPair.name);
		return s;
	}
	
}