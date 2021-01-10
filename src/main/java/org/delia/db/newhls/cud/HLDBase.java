package org.delia.db.newhls.cud;

import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;

public class HLDBase {
	public TypeOrTable typeOrTbl;
	public RelationInfo assocRelInfo; //null unless this is an assoc table 

    public HLDBase(TypeOrTable typeOrTbl) {
    	this.typeOrTbl = typeOrTbl;
    }

	public DStructType getStructType() {
		return typeOrTbl.getStructTypeEx(); //we know its never null
	}
}