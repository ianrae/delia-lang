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

/* A basic delia examples

 */
public class DeliaTests extends DeliaRunnerTestBase {

    @Test
    public void test() {
        DeliaLog log = new UnitTestLog();
        factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
        ConnectionDefinition connDef = ConnectionDefinitionBuilder.createMEM();

        Delia delia = DeliaFactory.create(connDef, log, factorySvc);
        String src = "let x = 5";

        DeliaSession sess = delia.beginSession(src);
        DValue dval = sess.getExecutionContext().varMap.get("x").getAsDValue();
        assertEquals(5, dval.asInt());
    }

    @Test
    public void test2() {
        DeliaLog log = new UnitTestLog();
        factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
        ConnectionDefinition connDef = ConnectionDefinitionBuilder.createMEM();

        Delia delia = DeliaFactory.create(connDef, log, factorySvc);
        String src = "type Customer struct {id int primaryKey, wid int, name string } wid.maxlen(4) end";

        DeliaSession sess = delia.beginSession(src);

        src = "insert Customer {id:1, wid:50, name:'a1'}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);

        src = "let x = Customer[1]";
        res = delia.continueExecution(src, sess);

        DValue dval = sess.getExecutionContext().varMap.get("x").getAsDValue();
        assertEquals(50, dval.asStruct().getField("wid").asInt());
    }

    @Test
    public void test2BadSyntax() {
        DeliaLog log = new UnitTestLog();
        factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
        ConnectionDefinition connDef = ConnectionDefinitionBuilder.createMEM();

        Delia delia = DeliaFactory.create(connDef, log, factorySvc);
        String src = "type Customer struct {id int primaryKey, wid int, name string } wid.maxlen(4) end";

        DeliaSession sess = delia.beginSession(src);

        src = "insert Customer {id:1, wid:50, name:a1}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(false, res.ok);
    }

    @Test
    public void testEscaping() {
        DeliaLog log = new UnitTestLog();
        factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
        ConnectionDefinition connDef = ConnectionDefinitionBuilder.createMEM();

        Delia delia = DeliaFactory.create(connDef, log, factorySvc);
        String src = "type Customer struct {id int primaryKey, wid int, name string } wid.maxlen(4) end";

        DeliaSession sess = delia.beginSession(src);

        src = "insert Customer {id:1, wid:50, name:'abc\\'de'}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);

        src = "Customer[true]";
        res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);
        DValue dval = res.getAsDValue();;
        String ss = dval.asStruct().getField("name").asString();
        assertEquals("abc\\'de", ss);
    }

    //---

    @Before
    public void init() {
    }
}