package org.delia.db.newhls.cond;

import java.util.ArrayList;
import java.util.List;

import org.delia.type.DStructType;

public class SymbolChain {
	public DStructType fromType;

	public SymbolChain(DStructType fromType) {
		this.fromType = fromType;
	}

	public List<String> list = new ArrayList<>();
}
