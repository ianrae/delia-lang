package org.delia.db.newhls.cud;

import org.delia.type.DStructType;

public class HLDBase {
	public TypeOrTable typeOrTbl;
	
    public HLDBase(TypeOrTable typeOrTbl) {
    	this.typeOrTbl = typeOrTbl;
    }

	public DStructType getStructType() {
		return typeOrTbl.getStructTypeEx(); //we know its never null
	}
}