package org.delia.zdb.h2;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBCapabilties;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBType;
import org.delia.db.h2.H2ErrorConverter;
import org.delia.db.sql.ConnectionFactory;
import org.delia.log.LogLevel;
import org.delia.log.SimpleLog;
import org.delia.zdb.ZDBConnection;
import org.delia.zdb.ZDBInterfaceFactory;

public class H2ZDBInterfaceFactory extends ServiceBase implements ZDBInterfaceFactory {
	private DBCapabilties capabilities;
	private SimpleLog sqlLog;
	private ConnectionFactory connFactory;
	private DBErrorConverter errorConverter;

	public H2ZDBInterfaceFactory(FactoryService factorySvc, ConnectionFactory connFactory) {
		super(factorySvc);
		this.capabilities = new DBCapabilties(true, true, true, true);
		this.sqlLog = new SimpleLog();
		this.connFactory = connFactory;
		this.errorConverter = new H2ErrorConverter();
		this.connFactory.setErrorConverter(errorConverter);
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
	public ZDBConnection openConnection() {
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
}