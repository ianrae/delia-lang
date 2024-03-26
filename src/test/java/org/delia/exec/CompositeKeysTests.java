package org.delia.exec;

import org.delia.ConnectionDefinitionBuilder;
import org.delia.DeliaFactory;
import org.delia.DeliaSession;
import org.delia.base.UnitTestLog;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.error.DeliaError;
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

        chkFound(sess, "1");
        chkNotFound(sess, "2");
    }

    @Test
    public void testMultiplePK() {
        String src = "type Customer struct {id int primaryKey, id2 string primaryKey, wid int optional, name string } wid.maxlen(4) end";
        DeliaSession sess = initDelia(src);

        src = "insert Customer {id:1, id2:'abc', wid:50, name:'a1'}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);

        chkFound(sess, "{1,'abc'}");
        chkNotFound(sess, "{2,'abc'}");
        chkNotFound(sess, "{1,'def'}");
    }

    @Test
    public void testSinglePKDeferred() {
        String src = "type Customer struct {id int primaryKey, wid int optional, name string } wid.maxlen(4) end";
        DeliaSession sess = initDelia(src);

        src = "let c = 1\n";
        src += "let c2 = 2\n";
        src += "insert Customer {id:1, wid:50, name:'a1'}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);

        chkFound(sess, "c");
        chkNotFound(sess, "c2");
    }

    @Test
    public void testMultiplePKDeferred() {
        String src = "type Customer struct {id int primaryKey, id2 string primaryKey, wid int optional, name string } wid.maxlen(4) end";
        DeliaSession sess = initDelia(src);

        src = "let c = 1\n";
        src += "let c2 = 2\n";
        src += "insert Customer {id:1, id2:'abc', wid:50, name:'a1'}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);

        chkFound(sess, "{c,'abc'}");
        chkNotFound(sess, "{c,'def'}");
        chkNotFound(sess, "{c2,'abc'}");
    }

    @Test
    public void testMultiplePKDeferred2() {
        String src = "type Customer struct {id int primaryKey, id2 string primaryKey, wid int optional, name string } wid.maxlen(4) end";
        DeliaSession sess = initDelia(src);

        //this time we'll use var in 2nd arg
        src = "let c = 'abc'\n";
        src += "let c2 = 2\n";
        src += "insert Customer {id:1, id2:'abc', wid:50, name:'a1'}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);

        chkFound(sess, "{1, c}");
        chkNotFound(sess, "{2, c}");
        chkNotFound(sess, "{1, c2}");
    }

    @Test
    public void testDelete() {
        String src = "type Customer struct {id int primaryKey, id2 string primaryKey, wid int optional, name string } wid.maxlen(4) end";
        DeliaSession sess = initDelia(src);

        src = "insert Customer {id:1, id2:'abc', wid:50, name:'a1'}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);

        chkFound(sess, "{1,'abc'}");
        chkDelete(sess, "{1, 'abc'}");
        chkNotFound(sess, "{1,'abc'}");
    }

    @Test
    public void testUpdate() {
        String src = "type Customer struct {id int primaryKey, id2 string primaryKey, wid int optional, name string } wid.maxlen(4) end";
        DeliaSession sess = initDelia(src);

        src = "insert Customer {id:1, id2:'abc', wid:50, name:'a1'}";
        ResultValue res = delia.continueExecution(src, sess);
        assertEquals(true, res.ok);

        chkFound(sess, "{1,'abc'}");
        chkUpdate(sess, "{1, 'abc'}", 60);
        chkFound(sess, "{1,'abc'}", 60);
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

    private void chkFound(DeliaSession sess, String arg) {
        buildAndRun(sess, arg);
        chkFound(sess, arg, 50);
    }
    private void chkFound(DeliaSession sess, String arg, int wid) {
        buildAndRun(sess, arg);
        DValue dval = sess.getExecutionContext().varMap.get("x").getAsDValue();
        assertEquals(wid, dval.asStruct().getField("wid").asInt());
    }

    private void chkNotFound(DeliaSession sess, String arg) {
        buildAndRun(sess, arg);
        DValue dval = sess.getExecutionContext().varMap.get("x").getAsDValue();
        assertEquals(null, dval);
    }
    private void chkDelete(DeliaSession sess, String arg) {
        String src = String.format("delete Customer[%s]", arg);
        doRun(sess, src);
    }
    private void chkUpdate(DeliaSession sess, String arg, int newWid) {
        String src = String.format("update Customer[%s] {id:1, id2:'abc', wid:%d, name:'a1'}", arg, newWid);
        doRun(sess, src);
    }

    private void buildAndRun(DeliaSession sess, String arg) {
        String src = String.format("let x = Customer[%s]", arg);
        doRun(sess, src);
    }
    private void doRun(DeliaSession sess, String src) {
        ResultValue res = delia.continueExecution(src, sess);

        if (! res.errors.isEmpty()) {
            log("errors: ");
            for(DeliaError err: res.errors) {
                log(err.toString());
            }
        }
        assertEquals(true, res.ok);
    }
}
