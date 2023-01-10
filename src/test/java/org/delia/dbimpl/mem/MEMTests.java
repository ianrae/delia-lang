package org.delia.dbimpl.mem;

import org.delia.DeliaOptions;
import org.delia.runner.ExecutionState;
import org.delia.runner.OuterRunner;
import org.delia.dbimpl.ExpTestHelper;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.lld.LLD;
import org.delia.lld.LLDBasicTestBase;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.runner.BasicRunnerResults;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class MEMTests extends LLDBasicTestBase {


    @Test
    public void test1() {
        //[true]
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());
        LLD.LLSelect lld = buildAndRunTwo(whereClause);
        chkLetStmt(lld, "id", "firstName");
        chkPK(lld, "id");
        chkJoins(lld, 0);
        chkWhere(lld, "[true]");
        chkSql(lld, "SELECT a.id, a.firstName FROM alpha.person as a");

        OuterRunner runner = createRunner();
        ExecutionState execState = runner.createNewExecutionState(mostRecentexecutable.registry);
        BasicRunnerResults res = runner.executeOnDBInterface(mostRecentexecutable, execState, new DeliaOptions(), true);
        assertEquals(null, res.insertResultVal);
        assertEquals(2, res.qresp.dvalList.size());
    }

    @Test
    public void test2() {
        //[true]
        Exp.WhereClause whereClause = ExpTestHelper.buildPKWhereClause(createValueBuilder(), "44");
        LLD.LLSelect lld = buildAndRunTwo(whereClause);

        OuterRunner runner = createRunner();
        ExecutionState execState = runner.createNewExecutionState(mostRecentexecutable.registry);
        BasicRunnerResults res = runner.executeOnDBInterface(mostRecentexecutable, execState, new DeliaOptions(), true);
        assertEquals(null, res.insertResultVal);
        assertEquals(0, res.qresp.dvalList.size());
    }
    @Test
    public void test2a() {
        //[true]
        Exp.WhereClause whereClause = ExpTestHelper.buildPKWhereClause(createValueBuilder(), "8");
        LLD.LLSelect lld = buildAndRunTwo(whereClause);

        OuterRunner runner = createRunner();
        ExecutionState execState = runner.createNewExecutionState(mostRecentexecutable.registry);
        BasicRunnerResults res = runner.executeOnDBInterface(mostRecentexecutable, execState, new DeliaOptions(), true);
        assertEquals(null, res.insertResultVal);
        assertEquals(1, res.qresp.dvalList.size());
        assertEquals("sue", res.qresp.getOne().asStruct().getField("firstName").asString());
    }

    @Test
    public void test3() {
        //[id < 10]
        Exp.WhereClause whereClause = ExpTestHelper.buildLTWhereClause(createValueBuilder(), "Person", "id", "8", null);
        LLD.LLSelect lld = buildAndRunTwo(whereClause);

        OuterRunner runner = createRunner();
        ExecutionState execState = runner.createNewExecutionState(mostRecentexecutable.registry);
        BasicRunnerResults res = runner.executeOnDBInterface(mostRecentexecutable, execState, new DeliaOptions(), true);
        assertEquals(null, res.insertResultVal);
        assertEquals(1, res.qresp.dvalList.size());
        assertEquals("bob", res.qresp.getOne().asStruct().getField("firstName").asString());
    }

    private OuterRunner createRunner() {
        MemDBInterfaceFactory dbInterface = new MemDBInterfaceFactory(factorySvc);
        OuterRunner runner = new OuterRunner(factorySvc, dbInterface, datSvc);
        return runner;
    }


    //---

    @Before
    public void init() {
        super.init();
    }

    protected LLD.LLSelect buildAndRunTwo(Exp.WhereClause whereClause) {
        ScalarValueBuilder valueBuilder = createValueBuilder();
        ExpTestHelper expHelper = new ExpTestHelper(factorySvc);
        AST.DeliaScript script = expHelper.buildScriptStartTwo(valueBuilder);
        AST.LetStatementAst letStmt = new AST.LetStatementAst();
        letStmt.typeName = mainTypeName;
        letStmt.whereClause = whereClause;
        script.add(letStmt);
        return buildAndRun(script, 5);
    }

}
