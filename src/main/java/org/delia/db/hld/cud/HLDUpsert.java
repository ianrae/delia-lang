package org.delia.db.hld.cud;

import org.delia.db.hld.HLDQuery;

public class HLDUpsert extends HLDUpdate {

	public boolean noUpdateFlag;

	public HLDUpsert(TypeOrTable typeOrTbl, HLDQuery hld) {
		super(typeOrTbl, hld);
	}

}
