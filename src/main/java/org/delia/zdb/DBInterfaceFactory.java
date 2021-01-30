package org.delia.zdb;

import org.delia.db.DBCapabilties;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBType;
import org.delia.hld.HLDFactory;

public interface DBInterfaceFactory {
	DBType getDBType();
	DBCapabilties getCapabilities();
	void setDBErrorConverter(DBErrorConverter errorConverter);
	DBErrorConverter getDBErrorConverter();
	
	DBConnection openConnection();
	DBExecutor createExecutor();
	
	boolean isSQLLoggingEnabled();
	void enableSQLLogging(boolean b);
	void setObserverFactory(DBObserverFactory observerFactory);
	DBObserverFactory getObserverFactory();
	void setIgnoreSimpleSvcSql(boolean flag);
	HLDFactory getHLDFactory();
}