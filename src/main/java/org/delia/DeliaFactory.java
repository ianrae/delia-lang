package org.delia;

import org.delia.api.DeliaImpl;
import org.delia.core.FactoryService;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.ConnectionFactoryImpl;
import org.delia.log.DeliaLog;
import org.delia.util.DeliaExceptionHelper;
import org.delia.db.DBInterfaceFactory;
import org.delia.dbimpl.mem.MemDBInterfaceFactory;
import org.delia.dbimpl.postgres.PostgresDBInterfaceFactory;

/**
 * Factory for creating Delia objects.
 *
 * @author Ian Rae
 */
public class DeliaFactory {
    //normally you don't need to provide a customer HLDFactory
    //	public static Delia create(ConnectionInfo info, Log log, FactoryService factorySvc) {
    //		return create(info, log, factorySvc, new HLDFactoryImpl());
    //	}

    //and now same methods with HLDFactory
    //	public static Delia create(ConnectionInfo info, Log log, FactoryService factorySvc, HLDFactory hldFactory) {
    //		ConnectionString connectionString = new ConnectionString();
    //		connectionString.jdbcUrl = info.jdbcUrl;
    //		connectionString.pwd = info.password;
    //		connectionString.userName = info.userName;
    //		return create(connectionString, info.dbType, log, factorySvc, hldFactory);
    //	}
    public static Delia create(ConnectionDefinition connectionDef, DeliaLog log, FactoryService factorySvc) {
        ConnectionFactory connFactory = new ConnectionFactoryImpl(connectionDef, log);

        DBInterfaceFactory dbInterface = null;
        switch (connectionDef.dbType) {
            case MEM:
                dbInterface = new MemDBInterfaceFactory(factorySvc);
                break;
            //            case H2:
            //                dbInterface = new H2DBInterfaceFactory(factorySvc, hldFactory, connFactory);
            //                break;
            case POSTGRES:
                dbInterface = new PostgresDBInterfaceFactory(factorySvc, connFactory); //, hldFactory, connFactory);
                break;
            default:
                DeliaExceptionHelper.throwError("unsupported-db-type", "Unknown DBType %s.", connectionDef.dbType == null ? "null" : connectionDef.dbType.name());
                break;
        }
        return create(dbInterface, log, factorySvc);
    }

    public static Delia create(DBInterfaceFactory dbInterface, DeliaLog log, FactoryService factorySvc) {
        return new DeliaImpl(dbInterface, log, factorySvc);
    }
}
