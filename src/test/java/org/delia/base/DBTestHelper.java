package org.delia.base;

import org.delia.core.FactoryService;
import org.delia.zdb.DBInterfaceFactory;
import org.delia.zdb.mem.MemDBInterfaceFactory;

public class DBTestHelper {

	//change this to true to disable all H2 and Postgres tests (they are slow)
	public static final boolean disableAllSlowTests = false;
	
	
	public static void throwIfNoSlowTests() {
		if (disableAllSlowTests) {
			throw new IllegalStateException("NO Slow tests!!!");
		}
	}
	
	public static DBInterfaceFactory createMEMDb(FactoryService factorySvc) {
		DBInterfaceFactory db = new MemDBInterfaceFactory(factorySvc);
		return db;
	}
	
}
