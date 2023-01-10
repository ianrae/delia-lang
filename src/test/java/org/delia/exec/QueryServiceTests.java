package org.delia.exec;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.core.QueryService;
import org.delia.db.DBType;
import org.delia.dbimpl.ExpTestHelper;
import org.delia.error.SimpleErrorTracker;
import org.delia.hld.DeliaExecutable;
import org.delia.runner.DeliaException;
import org.delia.runner.DeliaRunner;
import org.delia.runner.DeliaRunnerImpl;
import org.delia.runner.ResultValue;
import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.DTypeName;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class QueryServiceTests extends DeliaRunnerTestBase {

    @Test
    public void test() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        Delia delia = deliaRunner.getDelia();

        //build AST script
        ScalarValueBuilder valueBuilder = deliaRunner.createValueBuilder();
        FactoryService factorySvc = delia.getFactoryService();

        ExpTestHelper expHelper = new ExpTestHelper(factorySvc);
        AST.DeliaScript script = expHelper.buildScriptStart(valueBuilder);
        AST.LetStatementAst letStmt = new AST.LetStatementAst();
        letStmt.schemaName = "alpha";
        letStmt.typeName = "Person";

        letStmt.whereClause = ExpTestHelper.buildTrueWhereClause(valueBuilder);
        script.add(letStmt);

        DeliaExecutable executable = deliaRunner.buildExecutable(script);

        DeliaSession session = deliaRunner.execute(executable);
        DValue dval = session.getFinalResult().getAsDValue();
        assertEquals("bob", dval.asStruct().getField("firstName").asString());

        //and do continue
        script = new AST.DeliaScript();
        letStmt = new AST.LetStatementAst();
        letStmt.schemaName = "alpha";
        letStmt.typeName = "Person";

        letStmt.whereClause = ExpTestHelper.buildPKWhereClause(valueBuilder, "7");
        script.add(letStmt);

        executable = deliaRunner.buildExecutable(script);

        session = deliaRunner.execute(executable);
        dval = session.getFinalResult().getAsDValue();
        assertEquals("bob", dval.asStruct().getField("firstName").asString());
    }

    @Test
    public void testQueryService() {
        DeliaSession session = initSession();

        log("now querysvc");
        DeliaRunner deliaRunner = new DeliaRunnerImpl(session, true);
        ScalarValueBuilder valueBuilder = deliaRunner.createValueBuilder();

        QueryService querySvc = new QueryService(delia.getFactoryService(), deliaRunner);
        DTypeName dtypeName = new DTypeName("alpha", "Person");
        List<DValue> list = querySvc.queryAll(dtypeName);
        assertEquals(1, list.size());
        DValue dval = list.get(0);
        assertEquals("bob", dval.asStruct().getField("firstName").asString());

        dtypeName = new DTypeName("alpha", "Person");
        DStructType structType = session.getExecutionContext().registry.getStructType(dtypeName);
        DValue pkval = valueBuilder.buildInt(7);
        list = querySvc.queryPK(structType, pkval);
        assertEquals(1, list.size());

        pkval = valueBuilder.buildInt(777);
        list = querySvc.queryPK(structType, pkval);
        assertEquals(0, list.size());
    }

    @Test
    public void testStructType() {
        DeliaSession session = initSession();

        DTypeName dtypeName = new DTypeName(null, "NOSUCH NAME");
        DStructType structType = session.getExecutionContext().registry.getStructType(dtypeName);
        assertEquals(null, structType);
    }

    @Test(expected = DeliaException.class)
    public void testStructTypeFail() {
        DeliaSession session = initSession();

        DTypeName dtypeName = new DTypeName(null, "INTEGER_SHAPE");
        DStructType structType = session.getExecutionContext().registry.getStructType(dtypeName);
        assertEquals(null, structType);
    }


    @Test
    public void testCount() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        String src = "type Flight struct {id int primaryKey, name string } end";
        String src2 = "\nlet x = Flight[true].count()";

        AST.DeliaScript script = compileSrc(src + src2);
        DeliaExecutable executable = deliaRunner.buildExecutable(script);

        DeliaSession session = deliaRunner.execute(executable);
        ResultValue res = session.getFinalResult();
        assertEquals(0, res.getAsDValue().asInt());

    }

    //---

    @Before
    public void init() {
    }

    private DValue executeInNewRunner(AST.DeliaScript script, DeliaSession session) {
        DeliaRunnerImpl deliaRunner = new DeliaRunnerImpl(session, true);

        //build AST script
        AST.LetStatementAst letStmt = new AST.LetStatementAst();
        letStmt.typeName = "Person";

        ScalarValueBuilder valueBuilder = deliaRunner.createValueBuilder();
        letStmt.whereClause = ExpTestHelper.buildTrueWhereClause(valueBuilder);
        script.add(letStmt);

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        session = deliaRunner.execute(executable);
        DValue dval = session.getFinalResult().getAsDValue();
        return dval;
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
