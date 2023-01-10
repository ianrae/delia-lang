package org.delia.base;

import org.delia.db.DBExecutor;
import org.delia.db.DBInterfaceFactory;
import org.delia.type.DTypeName;

public class DBHelper {

	//Only works for mem db (it doesn't need registry for this)
	public static void createTable(DBInterfaceFactory dbInterface, DTypeName tableName) {
		//TODO hacky. fix this bad perf code

		try(DBExecutor zexec = dbInterface.createExecutor()) {
		    //TODO fix!!
			//zexec.execCreateTable(tableName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//dbInterface.createTable(tableName, null, null);
	}
}
