package org.delia.db.transaction;

import org.delia.db.DBCapabilties;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBType;
import org.delia.hld.HLDFactory;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zdb.DBConnection;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;
import org.delia.zdb.DBInterfaceFactoryInternal;
import org.delia.zdb.DBObserverFactory;

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
	public void setDBErrorConverter(DBErrorConverter errorConverter) {
		inner.setDBErrorConverter(errorConverter);
	}

	@Override
	public DBErrorConverter getDBErrorConverter() {
		return inner.getDBErrorConverter();
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

	@Override
	public void setIgnoreSimpleSvcSql(boolean flag) {
		inner.setIgnoreSimpleSvcSql(flag);
	}

	@Override
	public HLDFactory getHLDFactory() {
		return inner.getHLDFactory();
	}

	public TransactionAwareDBConnection getCurrentConn() {
		return currentConn;
	}

}
