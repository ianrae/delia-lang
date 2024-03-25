package org.delia.exec;

import org.delia.ConnectionDefinitionBuilder;
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

/**
 *  Limited Support for Composite keys
 *   -at most 2 primary keys
 *   -deferred: {1,c} and {c, 1} supported. {c,d} not supported
 *   -MM not supported. Many-to-Many relations must be between tables with single-key
 *
 */
public class CompositeKeysTests extends DeliaRunnerTestBase {

    @Test
    public void testSinglePK() {
        String src = "type Customer struct {id int primaryKey, wid int optional, name string } wid.maxlen(4) end";
        DeliaSession sess = initDelia(src);

        src = "insert Customer {id:1, wid:50, name:'a1'}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);

        src = "let x = Customer[1]";
        res = delia.continueExecution(src, sess);

        DValue dval = sess.getExecutionContext().varMap.get("x").getAsDValue();
        assertEquals(50, dval.asStruct().getField("wid").asInt());
    }


    @Test
    public void testMultiplePK() {
        String src = "type Customer struct {id int primaryKey, id2 string primaryKey, wid int optional, name string } wid.maxlen(4) end";
        DeliaSession sess = initDelia(src);

        src = "insert Customer {id:1, id2:'abc', wid:50, name:'a1'}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);

        src = "let x = Customer[{1,'abc'}]";
        res = delia.continueExecution(src, sess);

        DValue dval = sess.getExecutionContext().varMap.get("x").getAsDValue();
        assertEquals(50, dval.asStruct().getField("wid").asInt());
    }

    @Test
    public void testSinglePKDeferred() {
        String src = "type Customer struct {id int primaryKey, wid int optional, name string } wid.maxlen(4) end";
        DeliaSession sess = initDelia(src);

        src = "let c = 1\n";
        src += "insert Customer {id:1, wid:50, name:'a1'}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);

        src = "let x = Customer[c]";
        res = delia.continueExecution(src, sess);

        DValue dval = sess.getExecutionContext().varMap.get("x").getAsDValue();
        assertEquals(50, dval.asStruct().getField("wid").asInt());
    }

    @Test
    public void testMultiplePKDeferred() {
        String src = "type Customer struct {id int primaryKey, id2 string primaryKey, wid int optional, name string } wid.maxlen(4) end";
        DeliaSession sess = initDelia(src);

        src = "let c = 1\n";
        src += "insert Customer {id:1, id2:'abc', wid:50, name:'a1'}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);

        src = "let x = Customer[{c,'abc'}]";
        res = delia.continueExecution(src, sess);

        DValue dval = sess.getExecutionContext().varMap.get("x").getAsDValue();
        assertEquals(50, dval.asStruct().getField("wid").asInt());
    }

    @Test
    public void testMultiplePKDeferred2() {
        String src = "type Customer struct {id int primaryKey, id2 string primaryKey, wid int optional, name string } wid.maxlen(4) end";
        DeliaSession sess = initDelia(src);

        //this time we'll use var in 2nd arg
        src = "let c = 'abc'\n";
        src += "insert Customer {id:1, id2:'abc', wid:50, name:'a1'}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);

        src = "let x = Customer[{1, c}]";
        res = delia.continueExecution(src, sess);

        DValue dval = sess.getExecutionContext().varMap.get("x").getAsDValue();
        assertEquals(50, dval.asStruct().getField("wid").asInt());
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
