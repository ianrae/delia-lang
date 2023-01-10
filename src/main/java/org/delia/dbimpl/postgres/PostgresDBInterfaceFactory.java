package org.delia.dbimpl.postgres;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.*;
import org.delia.db.sql.ConnectionFactory;
import org.delia.dbimpl.mem.impl.FKResolver;
import org.delia.log.LogLevel;
import org.delia.log.SimpleLog;

public class PostgresDBInterfaceFactory extends ServiceBase implements DBInterfaceFactory, DBInterfaceFactoryInternal {
    private final ConnectionFactory connFactory;
    protected DBCapabilties capabilities;
    protected DBObserverFactory observerFactory;
    private SimpleLog sqlLog;
    private DBErrorConverter errorConverter;


    public PostgresDBInterfaceFactory(FactoryService factorySvc, ConnectionFactory connFactory) {
        super(factorySvc);
        this.capabilities = new DBCapabilties(true, true, true, true, "public");
        this.sqlLog = new SimpleLog();
        this.connFactory = connFactory;
        this.errorConverter = new PostgresErrorConverter(); //new SimpleSqlNameFormatter(null,true));
        this.connFactory.setErrorConverter(errorConverter);
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

    @Override
    public void setObserverFactory(DBObserverFactory observerFactory) {
        this.observerFactory = observerFactory;
    }

    @Override
    public DBObserverFactory getObserverFactory() {
        return observerFactory;
    }

    public DBErrorConverter getErrorConverter() {
        return errorConverter;
    }

    @Override
    public DBExecutor createExecutor() {
        DBConnection conn = (PostgresDBConnection) openConnection();
        return createExecutorEx(conn);
    }

    //    @Override
    public DBExecutor createExecutorEx(DBConnection conn) {
        SimpleLog execLog = new SimpleLog();
        execLog.setLevel(log.getLevel());
        FKResolver fkResolver = null;
        DBExecutor exec = new PostgresDBExecutor(factorySvc, sqlLog, this, conn, fkResolver, errorConverter);

        DBConnectionObserverAdapter connAdapter = null;
        if (observerFactory != null) {
            connAdapter = new DBConnectionObserverAdapter(conn);
            conn = connAdapter;
        }

        if (observerFactory != null) {
            DBExecutor observer = observerFactory.createObserver(exec, connAdapter, false);
            return observer;
        }
        return exec;
    }


//        @Override
//        public void setDBErrorConverter(DBErrorConverter errorConverter) {
//            //not used this.errorConverter = errorConverter;
//        }

}
