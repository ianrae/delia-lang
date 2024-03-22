package org.delia.sql;

import org.delia.ConnectionDefinitionBuilder;
import org.delia.Delia;
import org.delia.DeliaFactory;
import org.delia.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.base.TestBase;
import org.delia.base.UnitTestLog;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.error.SimpleErrorTracker;
import org.delia.lld.LLD;
import org.delia.log.DeliaLog;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultValueSqlTests extends TestBase {

    @Test
    public void test() {
        String src = "type Customer struct {id int primaryKey, wid int optional default(5), name string } wid.maxlen(4) end";
        DeliaSession sess = initDelia(src);

        String sql = null;
        DeliaSessionImpl sessimpl = (DeliaSessionImpl) sess;
        if (sessimpl.mostRecentExecutable != null) {
            for(LLD.LLStatement lldStatement: sessimpl.mostRecentExecutable.lldStatements) {
                if (lldStatement.getSql() != null) {
                    sql = lldStatement.getSql().sql;
                }
            }
        }

        log(sql);
        assertEquals(true, sql.contains("wid INTEGER DEFAULT 5,"));
    }

    //---
    private Delia delia;

    @Before
    public void init() {
    }

    private DeliaSession initDelia(String src) {
        DeliaLog log = new UnitTestLog();
        FactoryService factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
        ConnectionDefinition connDef = ConnectionDefinitionBuilder.createMEM();
        delia = DeliaFactory.create(connDef, log, factorySvc);
        delia.getOptions().generateSqlWhenMEMDBType = true;

        DeliaSession sess = delia.beginSession(src);
        return sess;
    }
}