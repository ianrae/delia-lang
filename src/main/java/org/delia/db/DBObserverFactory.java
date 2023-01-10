package org.delia.db;

public interface DBObserverFactory {
	DBExecutor createObserver(DBExecutor actual, DBConnectionObserverAdapter connAdapter, boolean ignoreSimpleSvcSql);
}
