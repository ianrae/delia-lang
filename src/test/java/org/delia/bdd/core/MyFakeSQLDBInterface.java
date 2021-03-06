package org.delia.bdd.core;

import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.db.h2.test.H2TestCleaner;
import org.delia.log.Log;

public class MyFakeSQLDBInterface extends InstrumentedZDBInterface {
	private Log log;
	public boolean cleanTables = true;
	public int execCount = 0;
	private boolean deferFlag;
	private boolean enableSQLLoggingFlag;
	public String tablesToClean;
	
	public MyFakeSQLDBInterface(DBType dbtype) {
		super(dbtype); 
	}

	@Override
	public void init(FactoryService factorySvc) {
		this.log = factorySvc.getLog();
		log.log("DBTYPE: %s", dbType.name());
		super.init(factorySvc);
		
		
		switch(dbType) {
		case MEM:
			break;
		case H2:
		{
			if (cleanTables) {
				H2TestCleaner cleaner = new H2TestCleaner(dbType);
				cleaner.deleteKnownTables(factorySvc, actualInterface);
			}
		}
		break;
		case POSTGRES:
		{
			if (cleanTables) {
				H2TestCleaner cleaner = new H2TestCleaner(dbType);
				cleaner.deleteKnownTables(factorySvc, actualInterface);
			}
		}
		break;
		}

//		if (deferFlag) {
//			DBInterfaceInternal dbi = (DBInterfaceInternal) actualInterface;
//			dbi.enablePrintStackTrace(enableSQLLoggingFlag);
//		}
		
		if (tablesToClean != null) {
			H2TestCleaner cleaner = new H2TestCleaner(dbType);
			cleaner.deleteTables(factorySvc, actualInterface, tablesToClean);
			tablesToClean = null; //is a one-shot. only do for one test
		}
	}

	@Override
	public void enableSQLLogging(boolean b) {
		if (actualInterface == null) {
			deferFlag = true;
			enableSQLLoggingFlag = b;
		} else {
			actualInterface.enableSQLLogging(b);
		}
	}
	
}