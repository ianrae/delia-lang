package org.delia.db.hls;

import org.delia.db.sql.fragment.MiniSelectFragmentParser;
import org.delia.type.DStructType;

public class AssocTblManager {
	public boolean flip = false;
	
	public String getTableFor(DStructType type1, DStructType type2, MiniSelectFragmentParser miniSelectParser) {
		return flip ? "AddressCustomerAssoc" : "CustomerAddressAssoc"; //type1 on left
	}
	public boolean isFlipped() {
		return flip;
	}
	public String getAssocField(DStructType type) {
		String tbl = flip ? "AddressCustomerAssoc" : "CustomerAddressAssoc"; //type1 on left
		if (tbl.startsWith(type.getName())) {
			return "leftv";
		} else {
			return "rightv";
		}
	}
}