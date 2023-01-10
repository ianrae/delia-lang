package org.delia.api;

import org.delia.Delia;
import org.delia.DeliaOptions;
import org.delia.DeliaSession;
import org.delia.core.FactoryService;
import org.delia.log.DeliaLog;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.delia.db.DBInterfaceFactory;
import org.delia.runner.DeliaSessionRunnerShim;

import java.io.BufferedReader;

public class DeliaImpl implements Delia {
    private DeliaLog log;
    private DBInterfaceFactory mainDBInterface;
    private FactoryService factorySvc;
    private DeliaOptions deliaOptions = new DeliaOptions();
//        private MigrationService migrationSvc; TODO: add migration support
//        private ErrorAdjuster errorAdjuster; TODO: do we need an error adjuster
    private DeliaSession mostRecentSess;
    public InjectedVarMap injectedVarMap = new InjectedVarMap();

    public DeliaImpl(DBInterfaceFactory dbInterface, DeliaLog log, FactoryService factorySvc) {
        this.log = log;
        this.mainDBInterface = dbInterface;
        this.factorySvc = factorySvc;
//            this.migrationSvc = factorySvc.createMigrationService(dbInterface);
//            this.errorAdjuster = new ErrorAdjuster();
    }

    @Override
    public ResultValue execute(String src) {
        return null;
    }

    @Override
    public DeliaSession beginSession(String src) {
        DeliaSessionRunnerShim shim = new DeliaSessionRunnerShim(factorySvc, injectedVarMap);
        DeliaSession sess = shim.beginSession(this, src);
        mostRecentSess = sess;
        return sess;
    }

    @Override
    public ResultValue continueExecution(String src, DeliaSession dbsess) {
        DeliaSessionRunnerShim shim = new DeliaSessionRunnerShim(factorySvc, injectedVarMap);
        DeliaSession sess = shim.continueSession(this, src, dbsess);
        mostRecentSess = sess;
        return sess.getFinalResult();
    }

    @Override
    public DeliaLog getLog() {
        return log;
    }

    @Override
    public FactoryService getFactoryService() {
        return factorySvc;
    }

    @Override
    public DeliaOptions getOptions() {
        return deliaOptions;
    }

    @Override
    public DBInterfaceFactory getDBInterface() {
        return mainDBInterface;
    }

    @Override
    public ResultValue execute(BufferedReader reader) {
        return null;
    }

    @Override
    public DeliaSession beginSession(BufferedReader reader) {
        return null;
    }

    @Override
    public ResultValue continueExecution(BufferedReader reader, DeliaSession dbsess) {
        return null;
    }

    @Override
    public void injectVar(String varName, DValue dval) {
        injectedVarMap.addVar(varName, dval);
    }


}
