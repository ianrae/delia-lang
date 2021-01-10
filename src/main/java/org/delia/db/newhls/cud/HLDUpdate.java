package org.delia.db.newhls.cud;

import org.delia.db.QuerySpec;
import org.delia.db.newhls.HLDQuery;
import org.delia.db.newhls.HLDQueryStatement;
import org.delia.runner.ConversionResult;

public class HLDUpdate {
	public HLDQuery hld;
	public HLDInsert hldinsert; //values
	
	public ConversionResult cres;
	public QuerySpec querySpec;

	public HLDUpdate(HLDQuery hld) {
		this.hld = hld;
	}

	@Override
	public String toString() {
		return hld == null ? "" : hld.toString();
	}
}