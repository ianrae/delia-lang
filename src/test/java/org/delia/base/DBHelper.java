package org.delia.base;

import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;

public class DBHelper {

	//Only works for mem db (it doesn't need registry for this)
	public static void createTable(DBInterfaceFactory dbInterface, String tableName) {
		//TODO hacky. fix this bad perf code
		
		try(DBExecutor zexec = dbInterface.createExecutor()) {
			zexec.createTable(tableName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//dbInterface.createTable(tableName, null, null);
	}
}
