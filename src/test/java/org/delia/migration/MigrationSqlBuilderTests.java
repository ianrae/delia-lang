package org.delia.migration;

import org.delia.db.SqlStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/*
Note. we generate sql here but don't assert any of it -- that is done in MigrationBDDTests
 */
public class MigrationSqlBuilderTests extends MigrationActionTestBase {

    @Test
    public void testNone() {
        String additionalSrc = "";
        String migrationSrc = buildMigration("");
        runAndChk("", additionalSrc, migrationSrc, 0, "");
    }

    @Test
    public void test0() {
        String additionalSrc = "type Customer struct {id int primaryKey, wid int, name string } end";
        String migrationSrc = buildMigration("");
        runAndChk("", additionalSrc, migrationSrc, 1, "+TBL(Customer):id,wid,name");
    }

    @Test
    public void test() {
        String additionalSrc = "";
        String migrationSrc = buildMigration("DROP Customer");
        runAndChk(SRC1, additionalSrc, migrationSrc, 1, "-TBL(Customer)");
    }

    @Test
    public void test2() {
        String additionalSrc = "";
        String migrationSrc = buildMigration("RENAME Customer to Cust2");
        runAndChk(SRC1, additionalSrc, migrationSrc, 1, "rTBL(Customer):Cust2");
    }

    @Test
    public void test3() {
        String additionalSrc = "";
        String migrationSrc = buildMigration("ALTER Customer DROP wid");
        runAndChk(SRC1, additionalSrc, migrationSrc, 1, "-FLD(Customer.wid)");
    }

    @Test
    public void test4() {
        String additionalSrc = "";
        String migrationSrc = buildMigration("ALTER Customer RENAME wid TO height");
        runAndChk(SRC1, additionalSrc, migrationSrc, 1, "rFLD(Customer.wid:height)");
    }

    @Test
    public void test4a() {
        String additionalSrc = "";
        String migrationSrc = buildMigration("ALTER Customer RENAME wid TO height");
        runAndChk(SRC2, additionalSrc, migrationSrc, 1, "rFLD(Customer.wid:height)");
    }

    @Test
    public void test5() {
        String additionalSrc = "";
        String migrationSrc = buildMigration("ALTER Customer ADD f int optional");
        runAndChk(SRC1, additionalSrc, migrationSrc, 1, "+FLD(Customer.f):O:NNN:int:0");
    }

    @Test
    public void test5a() {
        String additionalSrc = "";
        String migrationSrc = buildMigration("ALTER Customer ADD f int sizeof(16)");
        runAndChk(SRC1, additionalSrc, migrationSrc, 1, "+FLD(Customer.f)::NNN:int:16");
    }

    @Test
    public void test6() {
        String additionalSrc = "";
        String migrationSrc = buildMigration("ALTER Customer ALTER wid int optional");
        runAndChk(SRC1, additionalSrc, migrationSrc, 1, "mFLD(Customer.wid):+O:NNN:int:0");
    }

    @Test
    public void test7() {
        String additionalSrc = "";
        String migrationSrc = buildMigration("ALTER Customer ALTER relation addr Address optional many");
        runAndChk(SRC2, additionalSrc, migrationSrc, 1, "mFLD(Customer.addr)::NYN:Address:0");
    }
    @Test
    public void test7a() {
        String additionalSrc = "";
        String migrationSrc = buildMigration("ALTER Address ALTER relation cust Customer one optional");
        runAndChk(SRC2, additionalSrc, migrationSrc, 1, "mFLD(Address.cust):+O:YNN:Customer:0");
    }


    //---

    @Before
    public void init() {
        super.init();
    }

    @Override
    protected void generateSqlIfNeeded(SchemaMigration schemaMigration, MigrationActionBuilder migrationBuilder) {
        MigrationDDLGenerator generator = new MigrationDDLGenerator(log, schemaMigration.sess.getDelia());
        List<SqlStatement> list = generator.generateSql(schemaMigration, migrationBuilder);
        for (SqlStatement sql : list) {
            log("sql: " + sql.sql);
        }
    }

}