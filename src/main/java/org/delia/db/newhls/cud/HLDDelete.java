package org.delia.db.newhls.cud;

import org.delia.db.newhls.HLDQuery;

public class HLDDelete {
	public HLDQuery hld;
	
	public HLDDelete(HLDQuery hld) {
		this.hld = hld;
	}

	@Override
	public String toString() {
		return hld.toString();
	}
}