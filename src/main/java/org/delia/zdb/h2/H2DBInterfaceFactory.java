package org.delia.zdb.h2;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBCapabilties;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBType;
import org.delia.db.sql.ConnectionFactory;
import org.delia.hld.HLDFactory;
import org.delia.log.Log;
import org.delia.log.LogLevel;
import org.delia.log.SimpleLog;
import org.delia.zdb.DBObserverFactory;
import org.delia.zdb.DBConnection;
import org.delia.zdb.DBConnectionObserverAdapter;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;

public class H2DBInterfaceFactory extends ServiceBase implements DBInterfaceFactory {
	private DBCapabilties capabilities;
	private Log sqlLog;
	private ConnectionFactory connFactory;
	private DBErrorConverter errorConverter;
	private H2DeliaSessionCache sessionCache;
	private DBObserverFactory observerFactory;
	private boolean ignoreSimpleSvcSql;
	private HLDFactory hldFactory;

	public H2DBInterfaceFactory(FactoryService factorySvc, HLDFactory hldFactory, ConnectionFactory connFactory) {
		super(factorySvc);
		this.capabilities = new DBCapabilties(true, true, true, true);
		this.sqlLog = createNewLog();
		this.connFactory = connFactory;
		this.errorConverter = new H2ErrorConverter();
		this.connFactory.setErrorConverter(errorConverter);
		this.sessionCache = new H2DeliaSessionCache();
		this.hldFactory = hldFactory;
	}
	
	private Log createNewLog() {
		if (factorySvc != null) {
			if (factorySvc.getLogFactory() != null) {
				return factorySvc.getLogFactory().create("sqlH2");
			}
		}
		return new SimpleLog();
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
		H2DBConnection conn;
		conn = new H2DBConnection(factorySvc, connFactory, errorConverter);
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
		DBConnection conn = (H2DBConnection) openConnection();
		Log execLog = createNewLog();
		execLog.setLevel(sqlLog.getLevel());
		
		DBConnectionObserverAdapter connAdapter = null;
		if (observerFactory != null) {
			connAdapter = new DBConnectionObserverAdapter(conn);
			conn = connAdapter;
		}
		
		DBExecutor exec = new H2DBExecutor(factorySvc, execLog, this, hldFactory, conn, sessionCache);
		if (observerFactory != null) {
			DBExecutor observer = observerFactory.createObserver(exec, connAdapter, ignoreSimpleSvcSql);
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

	@Override
	public void setIgnoreSimpleSvcSql(boolean flag) {
		this.ignoreSimpleSvcSql = flag;
	}

	@Override
	public HLDFactory getHLDFactory() {
		return hldFactory;
	}

}