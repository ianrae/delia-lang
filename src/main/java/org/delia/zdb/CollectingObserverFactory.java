package org.delia.zdb;


public class CollectingObserverFactory implements DBObserverFactory {
	private DBObserverAdapter observer;

	@Override
	public DBExecutor createObserver(DBExecutor actual, DBConnectionObserverAdapter connAdapter, boolean ignoreSimpleSvcSql) {
		if (observer == null) {
			observer = new DBObserverAdapter(actual, ignoreSimpleSvcSql);
		} else {
			observer.setInner(actual);
		}
		
		if (ignoreSimpleSvcSql && connAdapter != null) {
			connAdapter.setEnableObserver(true);
			connAdapter.setStatementList(observer.getStatementList());
		}
		
		return observer;
	}

	public DBObserverAdapter getObserver() {
		return observer;
	}
	
}
