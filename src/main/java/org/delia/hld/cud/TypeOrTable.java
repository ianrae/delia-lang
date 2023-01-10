package org.delia.hld.cud;

import org.delia.type.DStructType;

public class TypeOrTable {
	private DStructType structType;
	private String tblName; //a table that doesn't represent a DStructType, such as CustomerAddressDat1
	public String alias;
	public boolean isAssocTbl;
	public String defaultSchema;
	
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
	private String renderTblName() {
		String tbl = (tblName != null) ? tblName : structType.getName();
		if (defaultSchema != null) {
			return String.format("%s.%s", defaultSchema, tbl);
		} else {
			return tbl;
		}
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
			return String.format(" %s", renderTblName());
		} else {
			return String.format(" %s as %s", renderTblName(), alias);
		}
	}
	
}
