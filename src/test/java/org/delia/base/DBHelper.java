package org.delia.base;

import org.delia.db.DBInterface;

public class DBHelper {

	//Only works for mem db (it doesn't need registry for this)
	public static void createTable(DBInterface dbInterface, String tableName) {
		dbInterface.createTable(tableName, null);
	}
	
}
