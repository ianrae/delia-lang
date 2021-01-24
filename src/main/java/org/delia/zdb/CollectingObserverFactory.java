package org.delia.zdb;


public class CollectingObserverFactory implements DBObserverFactory {
	private DBObserverAdapter observer;

	@Override
	public ZDBExecutor createObserver(ZDBExecutor actual) {
		observer = new DBObserverAdapter(actual);
		return observer;
	}

	public DBObserverAdapter getObserver() {
		return observer;
	}
	
}
