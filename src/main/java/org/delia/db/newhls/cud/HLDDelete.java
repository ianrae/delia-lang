package org.delia.db.newhls.cud;

import org.delia.db.newhls.HLDQuery;

public class HLDDelete extends HLDBase {
	public HLDQuery hld;
	
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