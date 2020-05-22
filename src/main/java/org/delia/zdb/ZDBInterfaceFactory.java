package org.delia.zdb;

import org.delia.db.DBCapabilties;
import org.delia.db.DBType;
import org.delia.type.TypeReplaceSpec;

public interface ZDBInterfaceFactory {
	DBType getDBType();
	DBCapabilties getCapabilities();
	
	ZDBConnection openConnection();
	ZDBExecutor createExecutor();
	
	boolean isSQLLoggingEnabled();
	void enableSQLLogging(boolean b);
	void performTypeReplacement(TypeReplaceSpec spec);
}