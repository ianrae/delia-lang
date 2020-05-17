package org.delia.api;

import org.delia.builder.ConnectionInfo;
import org.delia.core.FactoryService;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.h2.H2DBInterface;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.postgres.PostgresDBInterface;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.ConnectionFactoryImpl;
import org.delia.db.sql.ConnectionString;
import org.delia.log.Log;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.h2.H2ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;

/**
 * Factory for creating Delia objects.
 * 
 * @author Ian Rae
 *
 */
public class DeliaFactory {

	public static Delia create(ConnectionInfo info, Log log, FactoryService factorySvc) {
		ConnectionString connectionString = new ConnectionString();
		connectionString.jdbcUrl = info.jdbcUrl;
		connectionString.pwd = info.password;
		connectionString.userName = info.userName;
		return create(connectionString, info.dbType, log, factorySvc);
	}
	public static Delia create(ConnectionString connectionString, DBType dbType, Log log, FactoryService factorySvc) {
		ConnectionFactory connFactory = new ConnectionFactoryImpl(connectionString, log);
		
		ZDBInterfaceFactory dbInterface = null;
		switch(dbType) {
		case MEM:
			dbInterface = new MemZDBInterfaceFactory(factorySvc);
			((MemDBInterface)dbInterface).createTablesAsNeededFlag = true;
			break;
		case H2:
			dbInterface = new H2ZDBInterfaceFactory(factorySvc, connFactory);
			break;
//		case POSTGRES:  TODO fix this
//			dbInterface = new PostgresDBInterface(factorySvc, connFactory);
//			break;
		default:
			DeliaExceptionHelper.throwError("unsupported-db-type", "Unknown DBType %s.", dbType == null ? "null" : dbType.name());
			break;
		}
		return create(dbInterface, log, factorySvc);
	}
	
	public static Delia create(ZDBInterfaceFactory dbInterface, Log log, FactoryService factorySvc) {
		return new DeliaImpl(dbInterface, log, factorySvc);
	}
}