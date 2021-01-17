package org.delia.db.newhls;

import org.delia.db.QuerySpec;

/**
 * Main class to represent a query. Should have all info to render sql or to invoke MEM.
 * Goal is to eventually be able to cache this to avoid repeated creation.
 * @author ian
 *
 */
public class HLDQueryStatement extends HLDStatement {
	public HLDQuery hldquery;
	public QuerySpec querySpec;
	
	
	public HLDQueryStatement(HLDQuery hld) {
		this.hldquery = hld;
	}
	

	@Override
	public String toString() {
		return hldquery.toString();
	}
}