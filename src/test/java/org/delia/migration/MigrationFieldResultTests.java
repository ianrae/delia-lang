package org.delia.migration;

import org.apache.commons.lang3.StringUtils;
import org.delia.type.DStructType;
import org.delia.type.DTypeName;
import org.delia.type.TypePair;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/*

 */
public class MigrationFieldResultTests extends MigrationActionTestBase {


    @Test
    public void testNone() {
        String additionalSrc = "";
        String migrationSrc = buildMigration("");
        runAndChk(SRC1, additionalSrc, migrationSrc, 0, "");
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