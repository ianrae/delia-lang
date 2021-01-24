package org.delia.zdb;


public class CollectingObserverFactory implements DBObserverFactory {
	private DBObserverAdapter observer;

	@Override
	public ZDBExecutor createObserver(ZDBExecutor actual) {
		if (observer == null) {
			observer = new DBObserverAdapter(actual);
		} else {
			observer.setInner(actual);
		}
		return observer;
	}

	public DBObserverAdapter getObserver() {
		return observer;
	}
	
}
