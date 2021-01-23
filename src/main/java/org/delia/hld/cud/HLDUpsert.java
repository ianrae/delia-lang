package org.delia.hld.cud;

import org.delia.hld.HLDQuery;

public class HLDUpsert extends HLDUpdate {

	public boolean noUpdateFlag;

	public HLDUpsert(TypeOrTable typeOrTbl, HLDQuery hld) {
		super(typeOrTbl, hld);
	}

}
