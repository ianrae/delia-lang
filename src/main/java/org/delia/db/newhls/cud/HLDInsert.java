package org.delia.db.newhls.cud;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.newhls.HLDField;
import org.delia.runner.ConversionResult;
import org.delia.type.DStructType;
import org.delia.type.DValue;

public class HLDInsert {
	//public HLDQuery hld; later if we support INSERT INTO SELECT 
	public TypeOrTable typeOrTbl;
	
    public List<HLDField> fieldL = new ArrayList<>();
    public List<DValue> valueL = new ArrayList<>();
    public ConversionResult cres;
    
    public HLDInsert(TypeOrTable typeOrTbl) {
    	this.typeOrTbl = typeOrTbl;
    }

	@Override
	public String toString() {
		String s = String.format("%s: todo!!" ,  typeOrTbl.getTblName());
		return s;
	}

	public DStructType getStructType() {
		return typeOrTbl.getStructTypeEx(); //we know its never null
	}
}