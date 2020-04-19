package org.delia.bddnew.core;

import org.delia.core.FactoryService;
import org.delia.db.DBInterfaceInternal;
import org.delia.db.DBType;
import org.delia.db.InstrumentedDBInterface;
import org.delia.db.h2.H2DBInterface;
import org.delia.db.h2.test.H2TestCleaner;
import org.delia.db.postgres.PostgresDBInterface;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.ConnectionFactoryImpl;
import org.delia.h2.H2ConnectionHelper;
import org.delia.log.Log;
import org.delia.postgres.PostgresConnectionHelper;

public class MyFakeSQLDBInterface extends InstrumentedDBInterface {
	private Log log;
	public boolean cleanTables = true;
	public int execCount = 0;
	private DBType dbType;
	private boolean deferFlag;
	private boolean enableSQLLoggingFlag;
	public String tablesToClean;
	
	public MyFakeSQLDBInterface(DBType dbtype) {
		super(null); //set later
		this.dbType = dbtype;
	}

	@Override
	public void init(FactoryService factorySvc) {
		this.log = factorySvc.getLog();
		log.log("DBTYPE: %s", dbType.name());

		switch(dbType) {
		case MEM:
			break;
		case H2:
		{
			ConnectionFactory connFact = new ConnectionFactoryImpl(H2ConnectionHelper.getTestDB(), log);
			H2DBInterface h2db = new H2DBInterface(factorySvc, connFact);
			actualInterface = h2db;

			actualInterface.init(factorySvc);

			if (cleanTables) {
				H2TestCleaner cleaner = new H2TestCleaner();
				cleaner.deleteKnownTables(factorySvc, actualInterface);
			}
			
			h2db.enumerateAllConstraints(log);
		}
		break;
		case POSTGRES:
		{
			ConnectionFactory connFact = new ConnectionFactoryImpl(PostgresConnectionHelper.getTestDB(), log);
			PostgresDBInterface h2db = new PostgresDBInterface(factorySvc, connFact);
			actualInterface = h2db;

			actualInterface.init(factorySvc);

			if (cleanTables) {
				H2TestCleaner cleaner = new H2TestCleaner();
				cleaner.deleteKnownTables(factorySvc, actualInterface);
			}
		}
		break;
		}

		if (deferFlag) {
			DBInterfaceInternal dbi = (DBInterfaceInternal) actualInterface;
			dbi.enablePrintStackTrace(enableSQLLoggingFlag);
		}
		
		if (tablesToClean != null) {
			H2TestCleaner cleaner = new H2TestCleaner();
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