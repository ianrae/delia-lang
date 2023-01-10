package org.delia.dbimpl.mem;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.*;
import org.delia.dbimpl.mem.impl.FKResolver;
import org.delia.dbimpl.mem.impl.SerialProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemDBInterfaceFactory extends ServiceBase implements DBInterfaceFactory {
    protected DBCapabilties capabilities;
    //    private Map<String, MemDBTable> tableMap; //only one for new
    private MemTableMap tableMap; //only one for new
    private Map<String, SerialProvider.SerialGenerator> serialMap = new ConcurrentHashMap<>(); //key, nextId values
    protected DBObserverFactory observerFactory;
    private FKResolver fkResolver;

    public MemDBInterfaceFactory(FactoryService factorySvc) {
        super(factorySvc);
        this.capabilities = new DBCapabilties(false, false, false, false, "public");
    }

    //call between tests to simulate a clean empty db
    public void clearSingleMemDB() {
        tableMap = null;
    }

    public MemTableMap createSingleMemDB() {
        if (tableMap == null) {
            tableMap = new MemTableMap();
            fkResolver = new FKResolver(factorySvc, tableMap);
        }
        return tableMap;
    }

    @Override
    public DBType getDBType() {
        return DBType.MEM;
    }

    @Override
    public DBCapabilties getCapabilities() {
        return capabilities;
    }

    @Override
    public DBConnection openConnection() {
        return null; //no connection for MEM
    }

    @Override
    public boolean isSQLLoggingEnabled() {
        return false;
    }

    @Override
    public void enableSQLLogging(boolean b) {
        //not supported
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
    public DBExecutor createExecutor() {
        if (tableMap == null) {
            createSingleMemDB(); //create fkResolover
        }
        DBExecutor exec = new MemDBExecutor(factorySvc, this, fkResolver);

        //there is no dbconnection for MEM
        if (observerFactory != null) {
            DBExecutor observer = observerFactory.createObserver(exec, null, false);
            return observer;
        }
        return exec;
    }

//        @Override
//        public DBExecutor createExecutorEx(DBConnection conn) {
//            return createExecutor();
//        }


    public Map<String, SerialProvider.SerialGenerator> getSerialMap() {
        return serialMap;
    }

//        @Override
//        public DBErrorConverter getDBErrorConverter() {
//            return null;
//        }
//        @Override
//        public void setDBErrorConverter(DBErrorConverter errorConverter) {
//            //not used this.errorConverter = errorConverter;
//        }

}
