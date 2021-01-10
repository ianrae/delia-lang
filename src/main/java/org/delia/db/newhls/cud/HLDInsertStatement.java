package org.delia.db.newhls.cud;

import java.util.*;

public class HLDInsertStatement {
	public HLDInsert hldinsert;
	public List<HLDUpdate> updateL = new ArrayList<>();
	public List<HLDInsert> assocInsertL;
	
	@Override
	public String toString() {
		return hldinsert.toString();
	}
	
}
