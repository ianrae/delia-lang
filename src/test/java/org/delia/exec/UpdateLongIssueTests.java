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
 Shape.INTEGER can physically store a Long or an Integer in the dvalue.
 This is a problem for MEM orderByFunction which tries to use a TreeMap to sort, but fails when
  it sees a mixture of Long and Integer.

 */
public class UpdateLongIssueTests extends DeliaRunnerTestBase {

    @Test
    public void test() {
        DeliaLog log = new UnitTestLog();
        factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
        ConnectionDefinition connDef = ConnectionDefinitionBuilder.createMEM();

        Delia delia = DeliaFactory.create(connDef, log, factorySvc);
        String src0 = "type ColumnDef struct {\n" +
                "  id int primaryKey serial,\n" +
                "  rawDataSetId int,\n" +
                "  colIndex int\n" +
                "}\n" +
                "end\n";


        String src = src0;
        DeliaSession sess = delia.beginSession(src);
        assertEquals(null, sess.getSessionOptions()); //null in main sess

        src = "insert ColumnDef {rawDataSetId:100, colIndex: 55}";
        ResultValue res = delia.continueExecution(src, sess);

//        src = "let x = ColumnDef[rawDataSetId==100]";
        src = "let x = ColumnDef[true]";
        res = delia.continueExecution(src, sess);

        DValue dval = res.getAsDValue();
        assertEquals(55, dval.asStruct().getField("colIndex").asInt());
        DValue innerVal = dval.asStruct().getField("colIndex");
        Object obj = innerVal.getObject();
        assert(obj instanceof Integer);
//        assert(obj instanceof Long);

        //now update should not change colIndex from Integer to Long (that's the bug)
        src = "update ColumnDef[1] {colIndex: 56}";
        res = delia.continueExecution(src, sess);

//        src = "let x = ColumnDef[rawDataSetId==100]";
        src = "let x = ColumnDef[true]";
        res = delia.continueExecution(src, sess);

        dval = res.getAsDValue();
        assertEquals(56, dval.asStruct().getField("colIndex").asInt());
        innerVal = dval.asStruct().getField("colIndex");
        obj = innerVal.getObject();
        assert(obj instanceof Integer);
//        assert(obj instanceof Long);

    }


    //---

    @Before
    public void init() {
    }
}