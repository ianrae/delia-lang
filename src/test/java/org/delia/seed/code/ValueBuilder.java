package org.delia.seed.code;

import org.delia.Delia;
import org.delia.DeliaFactory;
import org.delia.DeliaSession;
import org.delia.base.DBTestHelper;
import org.delia.base.UnitTestLog;
import org.delia.core.FactoryServiceImpl;
import org.delia.error.DeliaError;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.zdb.DBInterfaceFactory;

import static org.junit.Assert.assertEquals;

public class ValueBuilder {

    // --
    protected Delia delia;
    protected DeliaSession sess = null;
    protected boolean addIdFlag;
    protected DBInterfaceFactory dbInterface;
    protected int nextVarNum = 1;
    protected Log log = new UnitTestLog();
    protected FactoryServiceImpl factorySvc;

    public void init() {
        addIdFlag = true;
        factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
        dbInterface = DBTestHelper.createMEMDb(factorySvc);
        delia = DeliaFactory.create(dbInterface, log, factorySvc);
//		DBHelper.createTable(dbInterface, "Address"); //!! fake schema
//		DBHelper.createTable(dbInterface, "Customer"); //!! fake schema
    }

    protected void enableAutoCreateTables() {
//		MemDBInterface memdb = (MemDBInterface) dbInterface;
//		memdb.createTablesAsNeededFlag = true;
    }

    public DStructType createCustomerType(String src) {
        execTypeStatement(src);
        DTypeRegistry registry = sess.getExecutionContext().registry;
        DStructType dtype = (DStructType) registry.getType("Customer");
        return dtype;
    }


    protected void createType(String type, String relField) {
        String src = createTypeSrc(type, relField);
        ResultValue res = execTypeStatement(src);
        chkResOK(res);
    }

    protected String createTypeSrc(String type, String relField) {
        String sid = addIdFlag ? String.format(" id int unique") : "";
        relField = sid.isEmpty() ? relField : ", " + relField;
        String src = String.format("type %s struct { %s %s} end", type, sid, relField);
        src += "\n";
        return src;
    }

    protected ResultValue execTypeStatement(String src) {
        if (sess != null) {
            log("rebuilding..");
        }
        sess = delia.beginSession(src);

        ResultValue res = sess.getFinalResult();
        chkResOK(res);
        return res;
    }

    protected void createTypeFail(String initialSrc, String type, String rel, String errId) {
        String sid = addIdFlag ? String.format(" id int unique") : "";
        String src = String.format("type %s struct { %s %s }  end", type, sid, rel);
        execTypeStatementFail(initialSrc + src, errId);
    }

    protected void execTypeStatementFail(String src, String errId) {
        boolean pass = false;
        try {
            execTypeStatement(src);
            pass = true;
        } catch (DeliaException e) {
            DeliaError err = e.getLastError();
            assertEquals(true, err.getId().contains(errId));
        }
        assertEquals(false, pass);
    }

    public ResultValue execStatement(String src) {
        assertEquals(true, sess != null);
        ResultValue res = delia.continueExecution(src, sess);
        chkResOK(res);
        return res;
    }

    protected void execStatementFail(String src, String errId) {
        assertEquals(true, sess != null);
        boolean pass = false;
        try {
            delia.continueExecution(src, sess);
        } catch (DeliaException e) {
            DeliaError err = e.getLastError();
            assertEquals(true, err.getId().equals(errId));
            pass = true;
        }
        assertEquals(true, pass);
    }

    protected void chkResFail(ResultValue res, String errId) {
        assertEquals(false, res.ok);
        DeliaError err = res.getLastError();
        assertEquals(true, err.getId().equals(errId));
    }

    protected void chkResOK(ResultValue res) {
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


    public DTypeRegistry getRegistry() {
        return sess.getExecutionContext().registry;
    }

    public DValue buildDVal(int id, String firstName) {
        MyEntity entity = new MyEntity();
        entity.fieldMap.put("id", id);
        entity.fieldMap.put("firstName", firstName);
        String typeName = "Customer";

        SeedDValueBuilder builder = new SeedDValueBuilder(sess, typeName);
        return builder.buildFromEntityEx(entity, typeName);
    }

    public DValue buildDValNoId(String firstName) {
        MyEntity entity = new MyEntity();
        entity.fieldMap.put("firstName", firstName);
        String typeName = "Customer";

        SeedDValueBuilder builder = new SeedDValueBuilder(sess, typeName);
        return builder.buildFromEntityEx(entity, typeName);
    }

}
