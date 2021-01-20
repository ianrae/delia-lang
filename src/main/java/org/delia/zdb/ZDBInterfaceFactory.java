package org.delia.zdb;

import org.delia.db.DBCapabilties;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBType;

public interface ZDBInterfaceFactory {
	DBType getDBType();
	DBCapabilties getCapabilities();
	void setDBErrorConverter(DBErrorConverter errorConverter);
	
	ZDBConnection openConnection();
	ZDBExecutor createExecutor();
	
	boolean isSQLLoggingEnabled();
	void enableSQLLogging(boolean b);
}