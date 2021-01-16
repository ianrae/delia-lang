package org.delia.db.newhls.cud;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.newhls.HLDField;
import org.delia.runner.ConversionResult;
import org.delia.type.DValue;

public class HLDInsert extends HLDBase {
	//public HLDQuery hld; later if we support INSERT INTO SELECT 
	
    public List<HLDField> fieldL = new ArrayList<>();
    public List<DValue> valueL = new ArrayList<>();
    public ConversionResult cres;
    
    public HLDInsert(TypeOrTable typeOrTbl) {
    	super(typeOrTbl);
    }

	@Override
	public String toString() {
		String s = String.format("%s:", typeOrTbl.getTblName());
		return s;
	}

	public boolean buildSuccessful() {
		return cres.dval != null;
	}
}