package org.delia.exec;

import org.delia.ConnectionDefinitionBuilder;
import org.delia.Delia;
import org.delia.DeliaFactory;
import org.delia.DeliaSession;
import org.delia.base.UnitTestLog;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.DeliaLog;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultValueTests extends DeliaRunnerTestBase {

    @Test
    public void test() {
        String src = "type Customer struct {id int primaryKey, wid int optional default(5), name string } wid.maxlen(4) end";
        DeliaSession sess =  initDelia(src);

        src = "insert Customer {id:1, wid:50, name:'a1'}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);

        src = "let x = Customer[1]";
        res = delia.continueExecution(src, sess);

        DValue dval = sess.getExecutionContext().varMap.get("x").getAsDValue();
        assertEquals(50, dval.asStruct().getField("wid").asInt());
    }

    @Test
    public void test2() {
//        String src = "type Customer struct {id int primaryKey, wid int optional parent, name string } wid.maxlen(4) end";
        String src = "type Customer struct {id int primaryKey, wid int optional default(5), name string } wid.maxlen(4) end";
        DeliaSession sess =  initDelia(src);

        src = "insert Customer {id:1, name:'a1'}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);

        src = "let x = Customer[1]";
        res = delia.continueExecution(src, sess);

        DValue dval = sess.getExecutionContext().varMap.get("x").getAsDValue();
        assertEquals(5, dval.asStruct().getField("wid").asInt());
    }

    //---

    @Before
    public void init() {
    }

    private DeliaSession initDelia(String src) {
        DeliaLog log = new UnitTestLog();
        factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
        ConnectionDefinition connDef = ConnectionDefinitionBuilder.createMEM();
        delia = DeliaFactory.create(connDef, log, factorySvc);

        DeliaSession sess = delia.beginSession(src);
        return sess;
    }
}