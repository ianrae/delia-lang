package org.delia.db;


public interface DBInterfaceFactory {
    DBType getDBType();
    DBCapabilties getCapabilities();
//    void setDBErrorConverter(DBErrorConverter errorConverter);
//    DBErrorConverter getDBErrorConverter();

    DBConnection openConnection();

    DBExecutor createExecutor();

    boolean isSQLLoggingEnabled();

    void enableSQLLogging(boolean b);
    void setObserverFactory(DBObserverFactory observerFactory);
    DBObserverFactory getObserverFactory();
}