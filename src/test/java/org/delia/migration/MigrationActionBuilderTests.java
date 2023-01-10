package org.delia.migration;

import org.junit.Before;
import org.junit.Test;

/*
 TODO
 -delia differ. compare two delia srcs. produce SchemaMigration
 -generate from migration (ALTERATIONS,ADDITIONS)
   -compile current src (delia1)
   -apply alterations and add additions -> delia2
   -compile delia2. compare LLCreateTable and LLCreateAssocTable with delia1 to find the additional tables
     -that is. LLCreateTable and LLCreateAssocTable in delia1 and apply alterations so any renames are don
       into a local list of typenames
       -then compare with delia2. if exist in delia2 but not in 1 then is an addition.
 -SQL generator. generate sql statements for actions

 */
public class MigrationActionBuilderTests extends MigrationActionTestBase {

    @Test
    public void testNone() {
        String additionalSrc = "";
        String migrationSrc = buildMigration("");
        runAndChk(SRC1, additionalSrc, migrationSrc, 0, "");
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
        String migrationSrc = buildMigration("ALTER Customer ADD f int sizeof(32)");
        runAndChk(SRC1, additionalSrc, migrationSrc, 1, "+FLD(Customer.f)::NNN:int:32");
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
        //do nothing
    }

}