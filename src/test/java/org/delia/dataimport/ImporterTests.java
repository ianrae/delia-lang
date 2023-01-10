package org.delia.dataimport;

import org.delia.DeliaSession;
import org.delia.base.TestBase;
import org.delia.base.UnitTestLog;
import org.delia.bddnew.core.BDDConnectionProvider;
import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.core.QueryService;
import org.delia.db.DBType;
import org.delia.dbimpl.ExpTestHelper;
import org.delia.hld.DeliaExecutable;
import org.delia.log.DeliaLog;
import org.delia.runner.DeliaRunner;
import org.delia.runner.DeliaRunnerImpl;
import org.delia.runner.ExpHelper;
import org.delia.type.DStructType;
import org.delia.type.DTypeName;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ImporterTests extends TestBase {


    @Test
    public void test() {
        DeliaSession session = initSession();

        DeliaRunner deliaRunner = new DeliaRunnerImpl(session, true);
        ScalarValueBuilder valueBuilder = deliaRunner.createValueBuilder();

        DTypeName dtypeName = new DTypeName("alpha", "Person");
        DStructType structType = session.getRegistry().getStructType(dtypeName); //createType("Person", session);

        List<DValue> values = new ArrayList<>();
        ExpHelper helper = new ExpHelper(session.getDelia().getFactoryService());
        StructValueBuilder builder = new StructValueBuilder(structType);
        builder.addField("id", valueBuilder.buildInt(10));
        builder.addField("firstName", helper.buildDValueString("bobby"));
        boolean b = builder.finish();
        values.add(builder.getDValue());

        builder = new StructValueBuilder(structType);
        builder.addField("id", valueBuilder.buildInt(11));
        builder.addField("firstName", helper.buildDValueString("sue"));
        builder.finish();
        values.add(builder.getDValue());

        ImporterService importSvc = new ImporterService(delia.getFactoryService(), deliaRunner);
        List<DValue> list = importSvc.insertValues(structType, values);
        assertEquals(null, list);

        QueryService querySvc = new QueryService(factorySvc, deliaRunner);
        dtypeName = new DTypeName("alpha", "Person");
        list = querySvc.queryAll(dtypeName);
        assertEquals(3, list.size());
        DValue dval = list.get(1);
        assertEquals("bobby", dval.asStruct().getField("firstName").asString());
    }

    //---

    @Before
    public void init() {
    }

    private DeliaRunnerImpl createRunner(DBType dbType) {
        DeliaLog log = new UnitTestLog();
        BDDConnectionProvider connProvider = new BDDConnectionProvider(dbType);
        DeliaRunnerImpl deliaRunner = new DeliaRunnerImpl(connProvider.getConnectionDef(), log);
        return deliaRunner;
    }

    private DeliaSession initSession() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        delia = deliaRunner.getDelia();

        //build AST script for types
        ScalarValueBuilder valueBuilder = deliaRunner.createValueBuilder();
        FactoryService factorySvc = delia.getFactoryService();

        ExpTestHelper expHelper = new ExpTestHelper(factorySvc);
        AST.DeliaScript script = expHelper.buildScriptStart(valueBuilder);
        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        DeliaSession session = deliaRunner.execute(executable);

        sess = session;
        factorySvc = delia.getFactoryService();
        return session;
    }


}
