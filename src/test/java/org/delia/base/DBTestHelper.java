package org.delia.base;

import org.delia.core.FactoryService;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.hls.HLSMemZDBInterfaceFactory;

public class DBTestHelper {

	//change this to true to disable all H2 and Postgres tests (they are slow)
	public static final boolean disableAllSlowTests = false;
	
	
	public static void throwIfNoSlowTests() {
		if (disableAllSlowTests) {
			throw new IllegalStateException("NO Slow tests!!!");
		}
	}
	
	public static ZDBInterfaceFactory createMEMDb(FactoryService factorySvc) {
		ZDBInterfaceFactory db = new HLSMemZDBInterfaceFactory(factorySvc);
		return db;
	}
	
}
