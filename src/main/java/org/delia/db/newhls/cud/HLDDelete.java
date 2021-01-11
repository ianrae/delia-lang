package org.delia.db.newhls.cud;

import org.delia.db.newhls.HLDQuery;
import org.delia.type.DValue;

public class HLDDelete extends HLDBase {
	public HLDQuery hld;
	public boolean useDeleteIn;
	public DValue deleteInDVal;
	public String mergeKey;
	public String mergeType;
	public String mergePKField;
	
	public HLDDelete(HLDQuery hld) {
		super(new TypeOrTable(hld.fromType));
		this.hld = hld;
	}
	public HLDDelete(TypeOrTable typeOrTbl) {
		super(typeOrTbl);
	}

	@Override
	public String toString() {
		return hld.toString();
	}
}