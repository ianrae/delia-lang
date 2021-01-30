package org.delia.bdd.core;

import org.delia.core.FactoryService;
import org.delia.db.DBCapabilties;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBType;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.ConnectionFactoryImpl;
import org.delia.h2.H2ConnectionHelper;
import org.delia.hld.HLDFactory;
import org.delia.hld.HLDFactoryImpl;
import org.delia.postgres.PostgresConnectionHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zdb.DBObserverFactory;
import org.delia.zdb.DBConnection;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;
import org.delia.zdb.h2.H2DBInterfaceFactory;
import org.delia.zdb.mem.MemDBInterfaceFactory;
import org.delia.zdb.postgres.PostgresDBInterfaceFactory;

public class InstrumentedZDBInterface implements DBInterfaceFactory {
	
	protected DBType dbType;
	protected DBInterfaceFactory actualInterface;
	protected HLDFactory hldFactory = new HLDFactoryImpl();
	
	public InstrumentedZDBInterface(DBType dbType) {
		this.dbType = dbType;
	}
	
	public void init(FactoryService factorySvc) {
		switch(dbType) {
		case MEM:
			//actualInterface = new MemZDBInterfaceFactory(factorySvc);
			actualInterface = new MemDBInterfaceFactory(factorySvc, hldFactory);
			break;
		case H2:
		{
			ConnectionFactory connFact = new ConnectionFactoryImpl(H2ConnectionHelper.getTestDB(), factorySvc.getLog());
			actualInterface = new H2DBInterfaceFactory(factorySvc, hldFactory, connFact);
		}
			break;
		case POSTGRES:
		{
			ConnectionFactory connFact = new ConnectionFactoryImpl(PostgresConnectionHelper.getTestDB(), factorySvc.getLog());
			actualInterface = new PostgresDBInterfaceFactory(factorySvc, hldFactory, connFact);
		}
			break;
		default:
			DeliaExceptionHelper.throwError("db not yet supported", "DBType %s not supported", dbType.name());
		}
	}
	public void init(DBInterfaceFactory actual) {
		actualInterface = actual;
	}

	@Override
	public DBType getDBType() {
		return dbType;
	}

	@Override
	public DBCapabilties getCapabilities() {
		return actualInterface.getCapabilities();
	}

	@Override
	public DBConnection openConnection() {
		return actualInterface.openConnection();
	}

	@Override
	public DBExecutor createExecutor() {
		return actualInterface.createExecutor();
	}

	@Override
	public boolean isSQLLoggingEnabled() {
		return actualInterface.isSQLLoggingEnabled();
	}

	@Override
	public void enableSQLLogging(boolean b) {
		actualInterface.enableSQLLogging(b);
	}

	@Override
	public void setDBErrorConverter(DBErrorConverter errorConverter) {
		actualInterface.setDBErrorConverter(errorConverter);
	}

	@Override
	public DBErrorConverter getDBErrorConverter() {
		return actualInterface.getDBErrorConverter();
	}

	@Override
	public void setObserverFactory(DBObserverFactory observerFactory) {
		actualInterface.setObserverFactory(observerFactory);
	}

	@Override
	public DBObserverFactory getObserverFactory() {
		return actualInterface.getObserverFactory();
	}

	@Override
	public void setIgnoreSimpleSvcSql(boolean flag) {
		actualInterface.setIgnoreSimpleSvcSql(flag);
	}

	@Override
	public HLDFactory getHLDFactory() {
		return hldFactory;
	}

}
