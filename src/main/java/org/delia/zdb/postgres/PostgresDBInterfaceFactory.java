package org.delia.zdb.postgres;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBCapabilties;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBType;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.SimpleSqlNameFormatter;
import org.delia.hld.HLDFactory;
import org.delia.log.LogLevel;
import org.delia.log.SimpleLog;
import org.delia.zdb.DBObserverFactory;
import org.delia.zdb.DBConnection;
import org.delia.zdb.DBConnectionObserverAdapter;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;
import org.delia.zdb.DBInterfaceFactoryInternal;

public class PostgresDBInterfaceFactory extends ServiceBase implements DBInterfaceFactory,DBInterfaceFactoryInternal {
	private DBCapabilties capabilities;
	private SimpleLog sqlLog;
	private ConnectionFactory connFactory;
	private DBErrorConverter errorConverter;
	private PostgresDeliaSessionCache sessionCache;
	private DBObserverFactory observerFactory;
	private boolean ignoreSimpleSvcSql;
	private HLDFactory hldFactory;

	public PostgresDBInterfaceFactory(FactoryService factorySvc, HLDFactory hldFactory, ConnectionFactory connFactory) {
		super(factorySvc);
		this.capabilities = new DBCapabilties(true, true, true);
		this.sqlLog = new SimpleLog();
		this.connFactory = connFactory;
		this.errorConverter = new PostgresErrorConverter(new SimpleSqlNameFormatter(true));
		this.connFactory.setErrorConverter(errorConverter);
		this.sessionCache = new PostgresDeliaSessionCache();
		this.hldFactory = hldFactory;
	}

	@Override
	public DBType getDBType() {
		return DBType.POSTGRES;
	}

	@Override
	public DBCapabilties getCapabilities() {
		return capabilities;
	}

	@Override
	public DBConnection openConnection() {
		PostgresDBConnection conn = new PostgresDBConnection(factorySvc, connFactory, errorConverter);
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
		DBConnection conn = (PostgresDBConnection) openConnection();
		return createExecutorEx(conn);
	}

	@Override
	public DBExecutor createExecutorEx(DBConnection conn) {
		SimpleLog execLog = new SimpleLog();
		execLog.setLevel(log.getLevel());
		DBExecutor exec = new PostgresDBExecutor(factorySvc, execLog, this, hldFactory, conn, sessionCache);
		
		DBConnectionObserverAdapter connAdapter = null;
		if (observerFactory != null) {
			connAdapter = new DBConnectionObserverAdapter(conn);
			conn = connAdapter;
		}
		
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