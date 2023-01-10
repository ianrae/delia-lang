package org.delia.base;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.core.FactoryServiceImpl;
import org.delia.error.DeliaError;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.DeliaLog;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.delia.db.DBInterfaceFactory;

import static org.junit.Assert.assertEquals;

public class TestBase {

    // --
    protected Delia delia;
    protected DeliaSession sess = null;
    protected boolean addIdFlag;
    protected DBInterfaceFactory deliaDbInterface;
    protected int nextVarNum = 1;
    protected DeliaLog log = new UnitTestLog(); //default log that gets overwritten in init()
    protected FactoryServiceImpl factorySvc;

    public void init() {
        addIdFlag = true;
        log = new UnitTestLog();
        factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
//        deliaDbInterface = DBTestHelper.createMEMDb(factorySvc);
//        delia = DeliaFactory.create(deliaDbInterface, log, factorySvc);
//		DBHelper.createTable(dbInterface, "Address"); //!! fake schema
//		DBHelper.createTable(dbInterface, "Customer"); //!! fake schema
    }

    protected ResultValue execTypeStatement(String src) {
        if (sess != null) {
            log("rebuilding..");
        }
        sess = delia.beginSession(src);

        ResultValue res = sess.getFinalResult();
        chkDeliaResOK(res);
        return res;
    }

    protected ResultValue execStatement(String src) {
        assertEquals(true, sess != null);
        ResultValue res = delia.continueExecution(src, sess);
        chkDeliaResOK(res);
        return res;
    }

    protected void chkDeliaResFail(ResultValue res, String errId) {
        assertEquals(false, res.ok);
        DeliaError err = res.getLastError();
        assertEquals(true, err.getId().equals(errId));
    }

    protected void chkDeliaResOK(ResultValue res) {
        assertEquals(true, res.ok);
        assertEquals(true, res.errors.isEmpty());
    }

    protected void log(String s) {
        log.log(s);
    }

    protected String buildLet() {
        return String.format("let x%d = ", nextVarNum++);
    }

    protected void chkNullField(ResultValue res, String fieldName) {
        DValue dval = res.getAsDValue();
        assertEquals(null, dval.asStruct().getField(fieldName));
    }


}
