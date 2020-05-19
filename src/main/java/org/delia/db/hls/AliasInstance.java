package org.delia.db.hls;

import org.delia.type.DStructType;

public class AliasInstance {
	public DStructType structType;
	public String assocTbl; //can be null
	public String instanceKey; //usually fieldName
	public String alias;
}
