package org.delia.db.hls;

import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;

public class TestCreatorHelper {

	public static void createTable(ZDBInterfaceFactory db, String tableName) {
		try(ZDBExecutor zexec = db.createExecutor()) {
			zexec.createTable(tableName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
