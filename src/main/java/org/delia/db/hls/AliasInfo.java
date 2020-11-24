package org.delia.db.hls;

import org.delia.type.DStructType;

public class AliasInfo {
	public String alias;
	public DStructType structType;
	public String fieldName;
	public DStructType tblType; //null if assoc table
	public String tblName;
	
	public String getAlias() {
		return alias;
	}
}