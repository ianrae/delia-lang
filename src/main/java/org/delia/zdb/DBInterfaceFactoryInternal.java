package org.delia.zdb;

public interface DBInterfaceFactoryInternal {
	DBExecutor createExecutorEx(DBConnection conn);
}