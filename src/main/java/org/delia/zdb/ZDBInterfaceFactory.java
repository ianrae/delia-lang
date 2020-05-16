package org.delia.zdb;

import org.delia.db.DBCapabilties;
import org.delia.db.DBType;

public interface ZDBInterfaceFactory {
	DBType getDBType();
	DBCapabilties getCapabilities();
	
	ZDBConnection openConnection();
	
	boolean isSQLLoggingEnabled();
	void enableSQLLogging(boolean b);
}