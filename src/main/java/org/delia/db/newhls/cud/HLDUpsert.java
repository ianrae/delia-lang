package org.delia.db.newhls.cud;

import org.delia.db.newhls.HLDQuery;

public class HLDUpsert extends HLDUpdate {

	public boolean noUpdateFlag;

	public HLDUpsert(TypeOrTable typeOrTbl, HLDQuery hld) {
		super(typeOrTbl, hld);
	}

}
