package org.delia.db.newhls.cond;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.newhls.JoinElement;
import org.delia.type.DStructType;

public class SymbolChain {
	public DStructType fromType;

	public SymbolChain(DStructType fromType) {
		this.fromType = fromType;
	}

	public List<String> list = new ArrayList<>();
	public JoinElement el; //can be null. if implicit join
}
