package org.delia.zdb.postgres;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBCapabilties;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBType;
import org.delia.db.h2.H2ErrorConverter;
import org.delia.db.postgres.PostgresErrorConverter;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.SimpleSqlNameFormatter;
import org.delia.log.LogLevel;
import org.delia.log.SimpleLog;
import org.delia.type.TypeReplaceSpec;
import org.delia.zdb.ZDBConnection;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.h2.H2ZDBConnection;

public class PostgresZDBInterfaceFactory extends ServiceBase implements ZDBInterfaceFactory {
	private DBCapabilties capabilities;
	private SimpleLog sqlLog;
	private ConnectionFactory connFactory;
	private DBErrorConverter errorConverter;
	private PostgresDeliaSessionCache sessionCache;

	public PostgresZDBInterfaceFactory(FactoryService factorySvc, ConnectionFactory connFactory) {
		super(factorySvc);
		this.capabilities = new DBCapabilties(true, true, true, true);
		this.sqlLog = new SimpleLog();
		this.connFactory = connFactory;
		this.errorConverter = new PostgresErrorConverter(new SimpleSqlNameFormatter(true));
		this.connFactory.setErrorConverter(errorConverter);
		this.sessionCache = new PostgresDeliaSessionCache();
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
	public ZDBConnection openConnection() {
		H2ZDBConnection conn = new H2ZDBConnection(factorySvc, connFactory, errorConverter);
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
	public void performTypeReplacement(TypeReplaceSpec spec) {
		//nothing to do
	}

	@Override
	public ZDBExecutor createExecutor() {
		H2ZDBConnection conn = (H2ZDBConnection) openConnection();
		SimpleLog execLog = new SimpleLog();
		execLog.setLevel(log.getLevel());
		return new PostgresZDBExecutor(factorySvc, execLog, this, conn, sessionCache);
	}
}