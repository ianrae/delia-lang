package org.delia.transaction;

import org.delia.db.*;
import org.delia.util.DeliaExceptionHelper;

public class TransactionAwareDBInterface implements DBInterfaceFactory {
	
	private DBInterfaceFactory inner;
	private TransactionAwareDBConnection currentConn; //one and only
	
	public TransactionAwareDBInterface(DBInterfaceFactory dbInterface) {
		this.inner = dbInterface;
	}

	@Override
	public DBType getDBType() {
		return inner.getDBType();
	}

	@Override
	public DBCapabilties getCapabilities() {
		return inner.getCapabilities();
	}


	@Override
	public DBConnection openConnection() {
		if (currentConn == null) {
			currentConn = new TransactionAwareDBConnection(inner.openConnection());
		}
		return currentConn;
	}

	@Override
	public DBExecutor createExecutor() {
		if (! (inner instanceof DBInterfaceFactoryInternal)) {
			DeliaExceptionHelper.throwNotImplementedError("DBType %s needs to support DBInterfaceFactoryInternal to have transactions", inner.getDBType().name());
		}
		DBInterfaceFactoryInternal dbint = (DBInterfaceFactoryInternal) inner;
		return dbint.createExecutorEx(currentConn);
	}

	@Override
	public boolean isSQLLoggingEnabled() {
		return inner.isSQLLoggingEnabled();
	}

	@Override
	public void enableSQLLogging(boolean b) {
		inner.enableSQLLogging(b);
	}

	@Override
	public void setObserverFactory(DBObserverFactory observerFactory) {
		inner.setObserverFactory(observerFactory);
	}

	@Override
	public DBObserverFactory getObserverFactory() {
		return inner.getObserverFactory();
	}


	public TransactionAwareDBConnection getCurrentConn() {
		return currentConn;
	}

}
