package org.delia.db.hls;

import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;

public class TestCreatorHelper {

	public static void createTable(DBInterfaceFactory db, String tableName) {
		try(DBExecutor zexec = db.createExecutor()) {
			zexec.createTable(tableName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
