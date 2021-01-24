package org.delia.zdb.h2;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBCapabilties;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBType;
import org.delia.db.h2.H2ErrorConverter;
import org.delia.db.sql.ConnectionFactory;
import org.delia.log.Log;
import org.delia.log.LogLevel;
import org.delia.log.SimpleLog;
import org.delia.zdb.DBObserverAdapter;
import org.delia.zdb.DBObserverFactory;
import org.delia.zdb.DBConnection;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;

public class H2ZDBInterfaceFactory extends ServiceBase implements DBInterfaceFactory {
	private DBCapabilties capabilities;
	private Log sqlLog;
	private ConnectionFactory connFactory;
	private DBErrorConverter errorConverter;
	private H2DeliaSessionCache sessionCache;
	private DBObserverFactory observerFactory;

	public H2ZDBInterfaceFactory(FactoryService factorySvc, ConnectionFactory connFactory) {
		super(factorySvc);
		this.capabilities = new DBCapabilties(true, true, true, true);
		this.sqlLog = createNewLog();
		this.connFactory = connFactory;
		this.errorConverter = new H2ErrorConverter();
		this.connFactory.setErrorConverter(errorConverter);
		this.sessionCache = new H2DeliaSessionCache();
	}
	
	private Log createNewLog() {
		if (factorySvc != null) {
			return factorySvc.getLogFactory().create("sqlH2");
		} else {
			return new SimpleLog();
		}
	}

	@Override
	public DBType getDBType() {
		return DBType.H2;
	}

	@Override
	public DBCapabilties getCapabilities() {
		return capabilities;
	}

	@Override
	public DBConnection openConnection() {
		H2ZDBConnection conn;
		conn = new H2ZDBConnection(factorySvc, connFactory, errorConverter);
		conn.openDB();
		return conn;
	}

	@Override
	public boolean isSQLLoggingEnabled() {
		return !LogLevel.OFF.equals(sqlLog.getLevel());
	}

	@Override
	public void enableSQLLogging(boolean b) {
		if (b) {
			sqlLog.setLevel(LogLevel.INFO);
		} else {
			sqlLog.setLevel(LogLevel.OFF);
		}
	}

	public DBErrorConverter getErrorConverter() {
		return errorConverter;
	}

	@Override
	public DBExecutor createExecutor() {
		H2ZDBConnection conn = (H2ZDBConnection) openConnection();
		Log execLog = createNewLog();
		execLog.setLevel(sqlLog.getLevel());
		DBExecutor exec = new H2ZDBExecutor(factorySvc, execLog, this, conn, sessionCache);
		
		if (observerFactory != null) {
			DBExecutor observer = observerFactory.createObserver(exec);
			return observer;
		}
		return exec;
	}

	@Override
	public DBErrorConverter getDBErrorConverter() {
		return errorConverter;
	}
	@Override
	public void setDBErrorConverter(DBErrorConverter errorConverter) {
		this.errorConverter = errorConverter;
	}

	@Override
	public void setObserverFactory(DBObserverFactory observerFactory) {
		this.observerFactory = observerFactory;
	}

	@Override
	public DBObserverFactory getObserverFactory() {
		return observerFactory;
	}

}