package org.delia.db.newhls.cud;

import org.delia.db.QuerySpec;
import org.delia.db.newhls.HLDQueryStatement;
import org.delia.runner.ConversionResult;

public class HLDUpdate {
	public HLDQueryStatement hld;
	public ConversionResult cres;
	public QuerySpec querySpec;

	public HLDUpdate(HLDQueryStatement hld) {
		this.hld = hld;
	}

	@Override
	public String toString() {
		return hld == null ? "" : hld.toString();
	}
}