package org.delia.bddnew;

import org.delia.bddnew.core.*;
import org.delia.db.DBType;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.log.Log;
import org.delia.util.TextFileReader;

import java.util.List;

import static org.junit.Assert.assertEquals;

public abstract class BDDTestBase { //extends SeedeTestBase {

    public static final String BASE_DIR = "./src/test/resources/test/bdd/";

    protected int singleTestToRunIndex = -1;

    protected Log seedeLog; //a custom log just for Seede execution
    protected Log deliaLog; //a custom log just for Delia execution
    protected Log log;

    protected abstract DBType getDBType();

    protected void runBBBTest(String baseDir, String bddFileName, int numTests) {
        BDDFeature feature = readTest(baseDir + bddFileName);

        DBType dbType = getDBType();
//        DbTableCleaner cleaner = new DbTableCleaner();
//        cleaner.cleanDB(dbType);

        if (seedeLog == null) {
            seedeLog = log;
        }
        if (deliaLog == null) {
            deliaLog = log;
        }

        BDDFeatureRunner runner = new BDDFeatureRunner(new BDDConnectionProvider(dbType), log);
        runner.addRunner(SnippetType.DELIA, new DeliaSnippetRunner(log, deliaLog));
        runner.addRunner(SnippetType.SQL, new SqlSnippetRunner(log));
        runner.addRunner(SnippetType.SEEDE, new SeedeSnippetRunner(log, seedeLog, deliaLog));
        runner.addRunner(SnippetType.VALUES, new ValuesSnippetRunner(log));
        if (singleTestToRunIndex >= 0) {
            runner.setSingleTestToRunIndex(singleTestToRunIndex);
            numTests = 1;
        }

        BDDFeatureResult res = runner.runTests(feature, bddFileName);
        log.log("finished: %s", bddFileName);
        assertEquals(numTests, res.numPass);
        assertEquals(0, res.numFail);
        assertEquals(0, res.numSkip);
    }

    //---
    protected void cleanDB(DBType dbType) {
//        if (DBType.H2.equals(dbType)) {
//            DbTableCleaner cleaner = new DbTableCleaner();
//            cleaner.clean(H2ConnectionHelper.getTestDB(), "PUBLIC");
//        } else if (DBType.POSTGRES.equals(dbType)) {
//            DbTableCleaner cleaner = new DbTableCleaner();
//            cleaner.clean(PostgresConnectionHelper.getTestDB(), "public");
//        }
    }
    protected void cleanDB(ConnectionDefinition connDef, String schemaName) {
//        DbTableCleaner cleaner = new DbTableCleaner();
//        cleaner.clean(connDef, schemaName);
    }

    protected BDDFeature readTest(String path) {
        TextFileReader r = new TextFileReader();
        List<String> lines = r.readFile(path);
        BDDFileParser parser = new BDDFileParser();
        return parser.parse(lines);
    }
    protected void runR300(String fileName, int numTests) {
        String baseDir = BASE_DIR + "R300-scalar/";
        runBBBTest(baseDir, fileName, numTests);
    }

}
