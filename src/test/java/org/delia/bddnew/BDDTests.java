package org.delia.bddnew;

import org.delia.base.UnitTestLog;
import org.delia.bddnew.core.*;
import org.delia.db.DBType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/*
FEATURE: CRUD assoc crud in many-to-many relations
background:
  ..steps.. such as create schema (eg Customer table)
  delia:
---
title: no assoc-crud
given1: json
given2: json
when:  RUN_SCRIPT(1)
       let x = Customer[true]
then:
  values

 */
public class BDDTests extends BDDTestBase { //extends SeedeTestBase {


    public static class MockSnippetRunner implements SnippetRunner {

        @Override
        public void setConnectionProvider(ConnectionProvider connProvider) {
        }

        @Override
        public BDDSnippetResult execute(BDDSnippet snippet, BDDSnippetResult previousRes) {
            BDDSnippetResult res = new BDDSnippetResult();
            res.ok = true;
            return res;
        }
    }

    @Test
    public void testParse() {
        String path = "./src/test/resources/test/bdd/sample1.txt";
        BDDFeature feature = readTest(path);
        assertEquals("let field value", feature.name);
        assertEquals(2, feature.testsL.size());
        BDDTest test = feature.testsL.get(0);
        assertEquals("function", test.title);
        test = feature.testsL.get(1);
        assertEquals("func missing", test.title);
    }

    @Test
    public void testRun() {
        String path = "./src/test/resources/test/bdd/sample1.txt";
        BDDFeature feature = readTest(path);

        BDDFeatureRunner runner = new BDDFeatureRunner(new BDDConnectionProvider(DBType.MEM), log);
        runner.addRunner(SnippetType.DELIA, new MockSnippetRunner());
        runner.addRunner(SnippetType.SQL, new MockSnippetRunner());
        runner.addRunner(SnippetType.SEEDE, new MockSnippetRunner());
        runner.addRunner(SnippetType.VALUES, new MockSnippetRunner());

        BDDFeatureResult res = runner.runTests(feature, "sample1.txt");
        assertEquals(2, res.numPass);
        assertEquals(0, res.numFail);
        assertEquals(0, res.numSkip);
    }

    //---
    @Before
    public void init() {
        UnitTestLog.disableLogging = false;
        //super.init();
    }

    @Override
    protected DBType getDBType() {
        return DBType.MEM;
    }


}
