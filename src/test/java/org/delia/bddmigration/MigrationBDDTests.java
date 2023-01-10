package org.delia.bddmigration;

import org.delia.ConnectionDefinitionBuilder;
import org.delia.DeliaFactory;
import org.delia.DeliaSession;
import org.delia.base.TestBase;
import org.delia.bddmigration.code.MigrationBDDParser;
import org.delia.bddmigration.code.MigrationBDDTest;
import org.delia.db.SqlStatement;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.migration.DeliaDiffer;
import org.delia.migration.MigrationActionBuilder;
import org.delia.migration.MigrationDDLGenerator;
import org.delia.migration.SchemaMigration;
import org.delia.migration.action.MigrationActionBase;
import org.delia.migration.code.MigrationInputs;
import org.delia.migrationparser.parser.MigrationParser;
import org.delia.migrationparser.parser.Token;
import org.delia.util.StrCreator;
import org.delia.util.StringTrail;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/*
TODO
 -add    * ALTER RULE action
 -still quite a few edge case scenarios not yet working
 */
public class MigrationBDDTests extends TestBase {

    @Test
    public void testM100() {
        runM100File("t0-add-type.txt", 1);
        runM100File("t0-remove-type.txt", 1);
        runM100File("t0-rename-type.txt", 2);
    }

    @Test
    public void testM200() {
        runM200File("t0-add-field.txt", 1);
        runM200File("t0-remove-field.txt", 1);
        runM200File("t0-rename-field.txt", 2);
        runM200File("t0-alter-field-optional-unique.txt", 1);
        runM200File("t0-alter-field-remove-optional-unique.txt", 1);
        runM200File("t0-alter-field-serial-primarykey.txt", 1);
        runM200File("t0-alter-field-type.txt", 1);
        runM200File("t0-alter-field-sizeof-int.txt", 1);
        runM200File("t0-alter-field-sizeof-string.txt", 1);
    }

    @Test
    public void testM300() {
        runM300File("t0-alter-field-int-to-relation.txt", 2);
        runM300File("t0-alter-field-relation-to-int.txt", 2);
        runM300File("t0-alter-field-relation-one-to-many.txt", 1);
        runM300File("t0-alter-field-relation-many-to-one.txt", 1);
    }

    @Test
    public void testM350() {
        runM350File("t0-add-field-mm-relation.txt", 3);
        runM350File("t0-remove-field-mm-relation.txt", 3);
        runM350File("t0-rename-field-mm-relation.txt", 4); //note does + and - of Dat table because doesn't recognize a rename
        runM350File("t0-alter-field-int-to-mm-relation.txt", 3);
        //TODO change cust sizeof
        //TODO change cust to optional or remove optional
    }

    @Test
    public void testM500() {
        runM500File("t0-add-type.txt", 1);
        runM500File("t0-drop-type.txt", 1);
        runM500File("t0-rename-type.txt", 1);
    }

    @Test
    public void testM600() {
        runM600File("t0-add-field.txt", 1);
        runM600File("t0-remove-field.txt", 1);
        runM600File("t0-rename-field.txt", 1);
        runM600File("t0-alter-field.txt", 1);
        runM600File("t0-alter-field-sizeof.txt", 1);
    }

    @Test
    public void testM700() {
        runM700File("t0-alter-field-int-to-relation.txt", 2);
        runM700File("t0-alter-field-relation-to-int.txt", 2);
        //TODO add a few more tests
    }

    @Test
    public void testM800() {
        runM800File("add-field-mm-relation.txt", 3);
        runM800File("t0-remove-field-mm-relation.txt", 3);
        runM800File("t0-rename-field-mm-relation.txt", 1);
        //TODO change cust sizeof
        //TODO int to mm or reverse
    }

    @Test
    public void testM1000() {
        runM1000File("t0-sql-add-type.txt", 1);
        runM1000File("t0-sql-drop-type.txt", 1);
        runM1000File("t0-sql-rename-type.txt", 1);
    }

    @Test
    public void testM1100() {
        runM1100File("t0-add-field.txt", 1);
        runM1100File("t0-remove-field.txt", 1);
        runM1100File("t0-rename-field.txt", 1);
        runM1100File("t0-alter-field.txt", 2);
        runM1100File("t0-alter-field-sizeof.txt", 1);
    }

    @Test
    public void testM1200() {
        runM1200File("t0-alter-field-int-to-relation.txt", 3);
        runM1200File("t0-alter-field-relation-to-int.txt", 1);
    }

    @Test
    public void testM1300() {
        runM1300File("add-field-mm-relation.txt", 1);
        runM1300File("t0-remove-field-mm-relation.txt", 1);
        runM1300File("t0-rename-field-mm-relation.txt", 0);
    }

    @Test
    public void testDebug() {
//        runM350File("t0-remove-field-mm-relation.txt", 3);
//        runM1100File("t0-remove-field.txt", 1);
        runM1300File("t0-remove-field-mm-relation.txt", 1);
    }


    //---

    @Before
    public void init() {
        super.init();
    }

    public static enum BDDGroup {
        M100_diff_type,
        M200_diff_scalar_field,
        M300_diff_relation,
        M350_diff_mm_relation,
        M500_action_type,
        M600_action_scalar_field,
        M700_action_relation,
        M800_action_mm_relation,
        M1000_sql_type,
        M1100_sql_scalar_field,
        M1200_sql_relation,
        M1300_sql_mm_relation
    }

    public static final String BASE_DIR = "./src/test/resources/test/bdd-migration/";

    protected int runM100File(String bddFileName, int numTests) {
        return runBBBTest(BDDGroup.M100_diff_type, bddFileName, numTests);
    }

    protected int runM200File(String bddFileName, int numTests) {
        return runBBBTest(BDDGroup.M200_diff_scalar_field, bddFileName, numTests);
    }

    protected int runM300File(String bddFileName, int numTests) {
        return runBBBTest(BDDGroup.M300_diff_relation, bddFileName, numTests);
    }

    protected int runM350File(String bddFileName, int numTests) {
        return runBBBTest(BDDGroup.M350_diff_mm_relation, bddFileName, numTests);
    }

    protected int runM500File(String bddFileName, int numTests) {
        return runBBBTest(BDDGroup.M500_action_type, bddFileName, false, false, numTests);
    }

    protected int runM600File(String bddFileName, int numTests) {
        return runBBBTest(BDDGroup.M600_action_scalar_field, bddFileName, false, false, numTests);
    }

    protected int runM700File(String bddFileName, int numTests) {
        return runBBBTest(BDDGroup.M700_action_relation, bddFileName, false, false, numTests);
    }

    protected int runM800File(String bddFileName, int numTests) {
        return runBBBTest(BDDGroup.M800_action_mm_relation, bddFileName, false, false, numTests);
    }

    protected int runM1000File(String bddFileName, int numTests) {
        return runBBBTest(BDDGroup.M1000_sql_type, bddFileName, false, true, numTests);
    }

    protected int runM1100File(String bddFileName, int numTests) {
        return runBBBTest(BDDGroup.M1100_sql_scalar_field, bddFileName, false, true, numTests);
    }

    protected int runM1200File(String bddFileName, int numTests) {
        return runBBBTest(BDDGroup.M1200_sql_relation, bddFileName, false, true, numTests);
    }

    protected int runM1300File(String bddFileName, int numTests) {
        return runBBBTest(BDDGroup.M1300_sql_mm_relation, bddFileName, false, true, numTests);
    }

    protected int runBBBTest(BDDGroup group, String bddFileName, int numActions) {
        return runBBBTest(group, bddFileName, true, false, numActions);
    }

    protected int runBBBTest(BDDGroup group, String bddFileName, boolean useDiffer, boolean genSql, int numActions) {
        log.log("FILE(%s): %s", group.name(), bddFileName);
        String dirName = group.name().replace('_', '-');
        String baseDir = BASE_DIR + dirName + "/";
        MigrationBDDParser parser = new MigrationBDDParser();
        parser.keepLineFeeds = true;
        MigrationBDDTest test = parser.readTest(baseDir + bddFileName);

        if (useDiffer) {
            DeliaDiffer differ = new DeliaDiffer(log);
            SchemaMigration schemaMigration = differ.compare(test.text1, test.text2);
            chkMigration(numActions, schemaMigration, test.text3);
        } else {
            ConnectionDefinition connDef = ConnectionDefinitionBuilder.createMEM();
            delia = DeliaFactory.create(connDef, log, factorySvc);
            DeliaSession sess = delia.beginSession(test.text1);
            MigrationActionBuilder migrationBuilder = new MigrationActionBuilder(log);
            MigrationInputs mi = buildMigrationAST(test.text2);
            SchemaMigration schemaMigration = migrationBuilder.updateMigration(sess, mi.asts, mi.additionalSrc);
            log("finalSrc: " + migrationBuilder.getFinalSource());

            if (genSql) {
                genAndCheckSql(schemaMigration, migrationBuilder, test, numActions);
            }
        }

        return 0;
    }

    private void genAndCheckSql(SchemaMigration schemaMigration, MigrationActionBuilder migrationBuilder, MigrationBDDTest test, int numActions) {
        MigrationDDLGenerator generator = new MigrationDDLGenerator(log, schemaMigration.sess.getDelia());
        List<SqlStatement> list = generator.generateSql(schemaMigration, migrationBuilder);
        StrCreator sc = new StrCreator();
        for (SqlStatement sql : list) {
            log("sql: " + sql.sql);
            String[] ar = sql.sql.split("\\n");
            for (int i = 0; i < ar.length; i++) {
                String s = ar[i].trim();
                if (!s.isEmpty()) {
                    sc.addStr(s);
                    sc.addStr(System.lineSeparator());
                }
            }
        }
        assertEquals(numActions, list.size());
        assertEquals(test.text3.trim(), sc.toString().trim());
    }

    private MigrationInputs buildMigrationAST(String migrationSrc) {
        MigrationParser parser = new MigrationParser(log);
        String additionalSrc = parser.findAdditions(migrationSrc);
        List<Token> tokens = parser.parseIntoTokens(migrationSrc);

        MigrationInputs mi = new MigrationInputs();
        mi.asts = null;
        if (tokens != null) {
            mi.asts = parser.parseIntoAST(tokens);
        }
        mi.migrationSrc = migrationSrc;
        mi.additionalSrc = parser.findAdditions(additionalSrc);
        return mi;
    }

    private void chkMigration(int n, SchemaMigration schemaMigration, String expected) {
        StringTrail trail = new StringTrail();
        for (MigrationActionBase action : schemaMigration.actions) {
            trail.add(action.toString());
        }
        log(trail.getTrail());
        assertEquals(n, schemaMigration.actions.size());
        assertEquals(expected, trail.getTrail());
    }

}