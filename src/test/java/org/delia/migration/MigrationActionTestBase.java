package org.delia.migration;

import org.delia.ConnectionDefinitionBuilder;
import org.delia.DeliaFactory;
import org.delia.DeliaSession;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.exec.DeliaRunnerTestBase;
import org.delia.migration.action.MigrationActionBase;
import org.delia.migration.code.MigrationInputs;
import org.delia.migrationparser.parser.MigrationParser;
import org.delia.migrationparser.parser.Token;
import org.delia.util.StrCreator;
import org.delia.util.StringTrail;

import java.util.List;

import static org.junit.Assert.assertEquals;

/*

 */
public abstract class MigrationActionTestBase extends DeliaRunnerTestBase {

    public static final String SRC1 = "type Customer struct {id int primaryKey, wid int, name string } wid.maxlen(4) end";
    public static final String SRC2 = "type Customer struct {id int primaryKey, wid int, name string, relation addr Address one optional parent } wid.maxlen(4) end"
            + "\ntype Address struct {id int primaryKey, wid int, relation cust Customer  one}  end";


    //---

    public void init() {
        super.init();
        ConnectionDefinition connDef = ConnectionDefinitionBuilder.createMEM();
        delia = DeliaFactory.create(connDef, log, factorySvc);
    }

    protected String buildMigration(String action) {
        StrCreator sc = new StrCreator();
        sc.o("// comment");
        sc.nl();
        sc.o("ALTERATIONS:");
        sc.nl();
        sc.addStr(action);
        sc.nl();
//        sc.o("REMOVE y");
        sc.o("ADDITIONS:");
        sc.nl();
        sc.o("Other stuff..");
        sc.nl();
        return sc.toString();
    }

    protected void runAndChk(String src, String additionalSrc, String migrationSrc, int n, String expected) {
        DeliaSession sess = delia.beginSession(src);
        MigrationActionBuilder migrationBuilder = new MigrationActionBuilder(log);
        MigrationInputs mi = buildMigrationAST(migrationSrc, additionalSrc);
        SchemaMigration schemaMigration = migrationBuilder.updateMigration(sess, mi.asts, mi.additionalSrc);
        log("finalSrc: " + migrationBuilder.getFinalSource());
        chkMigration(n, schemaMigration, expected);
        generateSqlIfNeeded(schemaMigration, migrationBuilder);
    }

    protected abstract void generateSqlIfNeeded(SchemaMigration schemaMigration, MigrationActionBuilder migrationBuilder);

    protected void chkMigration(int n, SchemaMigration schemaMigration, String expected) {
        StringTrail trail = new StringTrail();
        for (MigrationActionBase action : schemaMigration.actions) {
            trail.add(action.toString());
        }
        log(trail.getTrail());
        assertEquals(n, schemaMigration.actions.size());
        assertEquals(expected, trail.getTrail());
    }

    protected MigrationInputs buildMigrationAST(String migrationSrc, String additionalSrc) {
        MigrationParser parser = new MigrationParser(log);
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
}