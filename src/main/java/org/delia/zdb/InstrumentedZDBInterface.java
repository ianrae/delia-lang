package org.delia.zdb;

import org.delia.core.FactoryService;
import org.delia.db.DBCapabilties;
import org.delia.db.DBType;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.ConnectionFactoryImpl;
import org.delia.h2.H2ConnectionHelper;
import org.delia.postgres.PostgresConnectionHelper;
import org.delia.type.TypeReplaceSpec;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zdb.h2.H2ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.delia.zdb.postgres.PostgresZDBInterfaceFactory;

public class InstrumentedZDBInterface implements ZDBInterfaceFactory {
	
	protected DBType dbType;
	protected ZDBInterfaceFactory actualInterface;
	
	public InstrumentedZDBInterface(DBType dbType) {
		this.dbType = dbType;
	}
	
	public void init(FactoryService factorySvc) {
		switch(dbType) {
		case MEM:
			actualInterface = new MemZDBInterfaceFactory(factorySvc);
			break;
		case H2:
		{
			ConnectionFactory connFact = new ConnectionFactoryImpl(H2ConnectionHelper.getTestDB(), factorySvc.getLog());
			actualInterface = new H2ZDBInterfaceFactory(factorySvc, connFact);
		}
			break;
		case POSTGRES:
		{
			ConnectionFactory connFact = new ConnectionFactoryImpl(PostgresConnectionHelper.getTestDB(), factorySvc.getLog());
			actualInterface = new PostgresZDBInterfaceFactory(factorySvc, connFact);
		}
			break;
		default:
			DeliaExceptionHelper.throwError("db not yet supported", "DBType %s not supported", dbType.name());
		}
	}
	public void init(ZDBInterfaceFactory actual) {
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
	public ZDBConnection openConnection() {
		return actualInterface.openConnection();
	}

	@Override
	public ZDBExecutor createExecutor() {
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
	public void performTypeReplacement(TypeReplaceSpec spec) {
		actualInterface.performTypeReplacement(spec);
	}

}
