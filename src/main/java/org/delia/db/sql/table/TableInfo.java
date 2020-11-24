package org.delia.db.sql.table;

public class TableInfo {
	public String tblName; //creating table
	public String assocTblName; //can be null
	public String tbl1;
	public String tbl2;
	public String fieldName;
	
	public TableInfo(String tblName, String assocTblName) {
		this.tblName = tblName;
		this.assocTblName = assocTblName;
	}
}
