package org.delia.migration;

import org.delia.ConnectionDefinitionBuilder;
import org.delia.Delia;
import org.delia.DeliaFactory;
import org.delia.DeliaSession;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.SimpleLog;
import org.delia.migrationparser.DeliaSourceGenerator;
import org.delia.migrationparser.MigrationContext;
import org.delia.util.StrCreator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/*

 */
public class DeliaSourceGeneratorTests {


    @Test
    public void test() {
        org.delia.log.DeliaLog deliaLog = new SimpleLog();
        FactoryService factorySvc = new FactoryServiceImpl(deliaLog, new SimpleErrorTracker(deliaLog));
        ConnectionDefinition connDef = ConnectionDefinitionBuilder.createMEM();

        Delia delia = DeliaFactory.create(connDef, deliaLog, factorySvc);
        String src = buildSrc();
        DeliaSession sess = delia.beginSession(src);

        DeliaSourceGenerator gen = new DeliaSourceGenerator(new MigrationContext());
        String src2 = gen.render(sess.getRegistry());
        log(src);
        log("NEW:");
        log(src2);
        assertEquals(src, src2);
    }


    @Before
    public void init() {
    }

    private String buildSrc() {
        StrCreator sc = new StrCreator();
        sc.addStr("type Customer struct {");
        sc.nl();
        sc.addStr("  id int primaryKey,");
        sc.nl();
        sc.addStr("  wid int");
        sc.nl();
        sc.addStr("}");
        sc.nl();
        sc.addStr("wid.maxlen(4)");
        sc.nl();
        sc.addStr("end");
        sc.nl();
        return sc.toString();
    }
    private void log(String s) {
        System.out.println(s);
    }

}