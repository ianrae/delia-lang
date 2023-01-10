package org.delia.db;

public interface DBInterfaceFactoryInternal {
	DBExecutor createExecutorEx(DBConnection conn);
}