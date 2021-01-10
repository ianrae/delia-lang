package org.delia.db.newhls.cud;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.newhls.HLDField;
import org.delia.relation.RelationInfo;
import org.delia.runner.ConversionResult;
import org.delia.type.DStructType;
import org.delia.type.DValue;

public class HLDInsert extends HLDBase {
	//public HLDQuery hld; later if we support INSERT INTO SELECT 
	
    public List<HLDField> fieldL = new ArrayList<>();
    public List<DValue> valueL = new ArrayList<>();
    public ConversionResult cres;
	public RelationInfo assocRelInfo; //null unless this is an assoc table insert
    
    public HLDInsert(TypeOrTable typeOrTbl) {
    	super(typeOrTbl);
    }

	@Override
	public String toString() {
		String s = String.format("%s: todo!!" ,  typeOrTbl.getTblName());
		return s;
	}
}