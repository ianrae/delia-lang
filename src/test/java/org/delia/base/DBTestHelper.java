package org.delia.base;

import org.delia.ConnectionStringBuilder;
import org.delia.Delia;
import org.delia.builder.DeliaBuilder;
import org.delia.core.FactoryService;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.sql.ConnectionString;
import org.delia.hld.HLDFactoryImpl;
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
		DBInterfaceFactory db = new MemDBInterfaceFactory(factorySvc, new HLDFactoryImpl());
		return db;
	}
	
	public static Delia createNewDelia() {
		ConnectionString connStr = ConnectionStringBuilder.createMEM();
		Delia delia = DeliaBuilder.withConnection(connStr).build();
		return delia;
	}
	public static DeliaGenericDao createDao() {
		Delia delia = createNewDelia();
		return new DeliaGenericDao(delia);
	}
	
	
}
