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

/*
Discovered that if a delia insert statement fails with "NODATA: null field value for mandatory field" error,
that a subsequent delia query somehow still has that error in the res and fails
 */
public class SessionContinueTests extends DeliaRunnerTestBase {

    @Test
    public void test() {
        DeliaLog log = new UnitTestLog();
        factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
        ConnectionDefinition connDef = ConnectionDefinitionBuilder.createMEM();

        Delia delia = DeliaFactory.create(connDef, log, factorySvc);
        String src0 = "type Organization struct {\n" +
                "  id int primaryKey serial,\n" +
                "  name string unique,\n" +
                "  relation users Customer many optional\n" +
                "  }\n" +
                "end\n";


        String src = src0 + "type Customer struct {id int primaryKey, wid int, name string, relation org Organization one } wid.maxlen(4) end";
        DeliaSession sess = delia.beginSession(src);

        src = "insert Organization {name:'org1'}";
        ResultValue res = delia.continueExecution(src, sess);

        src = "insert Customer {id:1, wid:50, name:'a1'}";
        res = delia.continueExecution(src, sess);
//        assertEquals(true, res.ok);

        src = "let x = Organization[true].count()";
        res = delia.continueExecution(src, sess);

        DValue dval = sess.getExecutionContext().varMap.get("x").getAsDValue();
        assertEquals(50, dval.asStruct().getField("wid").asInt());
    }


    //---

    @Before
    public void init() {
    }
}