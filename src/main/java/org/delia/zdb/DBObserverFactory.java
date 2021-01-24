package org.delia.zdb;

public interface DBObserverFactory {
	ZDBExecutor createObserver(ZDBExecutor actual);
}
