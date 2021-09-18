//package org.delia.bddnew;
//
//import org.delia.db.DBType;
//import org.delia.seede.base.UnitTestLog;
//import org.delia.seede.bdd.core.*;
//import org.junit.Before;
//import org.junit.Test;
//
///**
// * TODO
// * -all shapes test
// * -Cust Addr 1:1
// * -Cust Addr M:1
// * -Cust School M:N
// * <p>
// * -H2
// * -Postgres
// */
//
///*
//FEATURE: CRUD assoc crud in many-to-many relations
//background:
//  ..steps.. such as create schema (eg Customer table)
//  delia:
//---
//title: no assoc-crud
//given1: json
//given2: json
//when:  RUN_SCRIPT(1)
//       let x = Customer[true]
//then:
//  values
//
// */
//public class MemBDDTests extends org.delia.seede.bdd.BDDTestBase {
//
//
//    @Test
//    public void testParse() {
//        String path = "./src/test/resources/test/bdd/R200-exist/t0-exist.txt";
//        BDDFeature feature = readTest(path);
//        assertEquals("exist", feature.name);
//        assertEquals(1, feature.testsL.size());
//        BDDTest test = feature.testsL.get(0);
//        assertEquals("test1", test.title);
//    }
//
//    @Test
//    public void testRun() {
//        String path = "./src/test/resources/test/bdd/R200-exist/t0-exist.txt";
//        BDDFeature feature = readTest(path);
//
//        BDDFeatureRunner runner = new BDDFeatureRunner(new BDDConnectionProvider(DBType.MEM), log);
//        runner.addRunner(SnippetType.DELIA, new DeliaSnippetRunner(log, log));
//        runner.addRunner(SnippetType.SQL, new org.delia.seede.bdd.BDDTests.MockSnippetRunner());
//        runner.addRunner(SnippetType.SEEDE, new SeedeSnippetRunner(log, log, log));
//        runner.addRunner(SnippetType.VALUES, new ValuesSnippetRunner(log));
//
//        BDDFeatureResult res = runner.runTests(feature, "t0-exist.txt");
//        assertEquals(1, res.numPass);
//        assertEquals(0, res.numFail);
//        assertEquals(0, res.numSkip);
//    }
//
//    //---
//    @Before
//    public void init() {
//        UnitTestLog.disableLogging = false;
//        super.init();
//    }
//
//    @Override
//    protected DBType getDBType() {
//        return DBType.MEM;
//    }
//}
