package org.delia.bdd.core;

import org.delia.core.FactoryService;
import org.delia.db.DBInterfaceInternal;
import org.delia.db.DBType;
import org.delia.db.h2.test.H2TestCleaner;
import org.delia.log.Log;
import org.delia.zdb.InstrumentedZDBInterface;

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
//		case POSTGRES:
//		{
//			ConnectionFactory connFact = new ConnectionFactoryImpl(PostgresConnectionHelper.getTestDB(), log);
//			PostgresDBInterface pgdb = new PostgresDBInterface(factorySvc, connFact);
//			actualInterface = pgdb;
//			//pgdb.useFragmentParser = useFragmentParser;
//			actualInterface.init(factorySvc);
//
//			if (cleanTables) {
//				H2TestCleaner cleaner = new H2TestCleaner(dbType);
//				cleaner.deleteKnownTables(factorySvc, actualInterface);
//			}
//		}
//		break;
		}

		if (deferFlag) {
			DBInterfaceInternal dbi = (DBInterfaceInternal) actualInterface;
			dbi.enablePrintStackTrace(enableSQLLoggingFlag);
		}
		
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