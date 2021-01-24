package org.delia.zdb;

public interface DBObserverFactory {
	DBExecutor createObserver(DBExecutor actual, DBConnectionObserverAdapter connAdapter, boolean ignoreSimpleSvcSql);
}
