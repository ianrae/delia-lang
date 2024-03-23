package org.delia.bddnew;

import org.delia.base.UnitTestLog;
import org.delia.bddnew.core.*;
import org.delia.db.DBType;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.log.DeliaLog;
import org.delia.util.TextFileReader;

import java.util.List;

import static org.junit.Assert.assertEquals;

public abstract class BDDTestBase { //extends SeedeTestBase {
    public static enum BDDGroup {
        R100_comments,
        R200_package,
        R300_scalar,
        R400_struct,
        R420_default,
        R500_relation,
        R550_multi_relation,
        R560_self_relation,
        R600_rules,
        R650_rule_fns,
        R660_rule_unique,
        R670_rule_index,
        R700_crud_insert,
        R800_crud_delete,
        R900_crud_update,
        R950_crud_assoc_crud,
        R1000_crud_upsert,
        R1100_userfn,
        R1200_let_scalar,
        R1300_let_query,
        R1350_filter_expr,
        R1400_let_filterfn,
        R1500_let_queryfn,
        R1550_let_queryfn_relation,
        R1600_let_fetch,
        R1700_let_field_value,
        R1800_let_dollardollar,
        R1900_let_return,
        R2000_sprig,
        R2100_migration,
        R2150_migration_relations,
        R2200_security,
        R2300_multi_relation,
        R2400_log,
        //R2500 input fn
        R2600_sizeof,
        R2650_date_only,
        R2700_blob,
        R2800_schema
    }

    //change this to true to disable all H2 and Postgres tests (they are slow)
    public static final boolean disableAllSlowTests = false;

    public static final String BASE_DIR = "./src/test/resources/test/bdd/";

    protected int singleTestToRunIndex = -1;
    protected boolean enableAllFileCheck = true; //TODO

    protected DeliaLog seedeLog; //a custom log just for Seede execution
    protected DeliaLog deliaLog; //a custom log just for Delia execution
    protected DeliaLog log = new UnitTestLog();

    protected abstract DBType getDBType();

    /**
     * When we want to run all unit tests but not have to wait
     * 15 minutes for H2 and Postgress BDD tests to run,
     * set disableAllSlowTests to true. They will fail immediately.
     */
    protected void disableAllSlowTestsIfNeeded() {
        if (BDDTestBase.disableAllSlowTests) {
            throw new IllegalArgumentException("disable SLOW tests");
        }
    }

    protected int runBBBTest(BDDGroup group, String bddFileName, int numTests) {
        String dirName = group.name().replace('_', '-');
        String baseDir = BASE_DIR + dirName + "/";
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
//        runner.addRunner(SnippetType.SEEDE, new SeedeSnippetRunner(log, seedeLog, deliaLog));
        runner.addRunner(SnippetType.VALUES, new ValuesSnippetRunner(log));
        if (singleTestToRunIndex >= 0) {
            runner.setSingleTestToRunIndex(singleTestToRunIndex);
            numTests = 1;
        }

        BDDFeatureResult res = runner.runTests(feature, bddFileName);
        log.log("finished: %s", bddFileName);
        assertEquals(numTests, res.numPass);
        assertEquals(0, res.numFail);
//        assertEquals(0, res.numSkip);
        return res.numPass;
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
    //a test that is part of a group but will be tested separately
    protected void ignoreTest(String filename) {
//        filesExecutedL.add(filename);
    }

//    protected void xxxrunR300(String fileName, int numTests) {
//        String baseDir = BASE_DIR + "R300-scalar/";
//        runBBBTest(baseDir, fileName, numTests);
//    }
    protected int runR300File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R300_scalar, filename, numTests);
    }
    protected int runR400File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R400_struct, filename, numTests);
    }
    protected int runR420File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R420_default, filename, numTests);
    }
    protected int runR500File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R500_relation, filename, numTests);
    }
    protected int runR550File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R550_multi_relation, filename, numTests);
    }
    protected int runR560File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R560_self_relation, filename, numTests);
    }
    protected int runR600File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R600_rules, filename, numTests);
    }
    protected int runR650File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R650_rule_fns, filename, numTests);
    }
    protected int runR660File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R660_rule_unique, filename, numTests);
    }
    protected int runR670File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R670_rule_index, filename, numTests);
    }
    protected int runR700File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R700_crud_insert, filename, numTests);
    }
    protected int runR800File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R800_crud_delete, filename, numTests);
    }
    protected int runR900File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R900_crud_update, filename, numTests);
    }
    protected int runR950File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R950_crud_assoc_crud, filename, numTests);
    }
    protected int runR1000File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R1000_crud_upsert, filename, numTests);
    }
    protected int runR1100File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R1100_userfn, filename, numTests);
    }
    protected int runR1200File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R1200_let_scalar, filename, numTests);
    }
    protected int runR1300File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R1300_let_query, filename, numTests);
    }
    protected int runR1350File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R1350_filter_expr, filename, numTests);
    }
    protected int runR1400File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R1400_let_filterfn, filename, numTests);
    }
    protected int runR1500File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R1500_let_queryfn, filename, numTests);
    }
    protected int runR1550File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R1550_let_queryfn_relation, filename, numTests);
    }
    protected int runR1600File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R1600_let_fetch, filename, numTests);
    }
    protected int runR1700File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R1700_let_field_value, filename, numTests);
    }
    protected int runR1800File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R1800_let_dollardollar, filename, numTests);
    }
    protected int runR1900File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R1900_let_return, filename, numTests);
    }
    protected int runR2000File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R2000_sprig, filename, numTests);
    }
    protected int runR2100File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R2100_migration, filename, numTests);
    }
    protected int runR2150File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R2150_migration_relations, filename, numTests);
    }
    protected int runR2200File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R2200_security, filename, numTests);
    }
    protected int runR2300File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R2300_multi_relation, filename, numTests);
    }
    protected int runR2400File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R2400_log, filename, numTests);
    }
    protected int runR2600File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R2600_sizeof, filename, numTests);
    }
    protected int runR2650File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R2650_date_only, filename, numTests);
    }
    protected int runR2700Blob(String filename, int numTests) {
        return runBBBTest(BDDGroup.R2700_blob, filename, numTests);
    }
    protected int runR2800File(String filename, int numTests) {
        return runBBBTest(BDDGroup.R2800_schema, filename, numTests);
    }

}
