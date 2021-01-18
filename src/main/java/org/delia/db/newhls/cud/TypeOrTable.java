package org.delia.db.newhls.cud;

import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;

public class TypeOrTable {
	private DStructType structType;
	private String tblName; //a table that doesn't represent a DStructType, such as CustomerAddressDat1
	public String alias;
	public boolean isAssocTbl;
	
	public TypeOrTable(DStructType structType) {
		this.structType = structType;
	}
	public TypeOrTable(String tblName, boolean isAssocTbl) {
		this.tblName = tblName;
		this.isAssocTbl = isAssocTbl;
	}
	
	public String getTblName() {
		return (tblName != null) ? tblName : structType.getName();
	}
	
	//may return null
	public DStructType getStructTypeEx() {
		return structType;
	}
	public String getAlias() {
		return alias;
	}
	
	public String render() {
		if (alias == null) {
			return String.format(" %s", getTblName());
		} else {
			return String.format(" %s as %s", getTblName(), alias);
		}
	}
	
}
