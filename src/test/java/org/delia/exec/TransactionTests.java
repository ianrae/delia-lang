package org.delia.exec;

import org.delia.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.dbimpl.ExpTestHelper;
import org.delia.hld.DeliaExecutable;
import org.delia.runner.DeliaRunner;
import org.delia.runner.DeliaRunnerImpl;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class TransactionTests extends DeliaRunnerTestBase {


    @Test
    public void test() {
        DeliaSession session = initSession(DBType.MEM);

        log("now querysvc");
        DeliaRunner deliaRunner = new DeliaRunnerImpl(session, true);
        ScalarValueBuilder valueBuilder = deliaRunner.createValueBuilder();

        String src = "let x = Person[7]";
        DeliaSession tmp = session.runInTransaction(() -> {
            return continueExecution(src);
        });
        DValue dval = session.getFinalResult().getAsDValue();
        assertEquals("bob", dval.asStruct().getField("firstName").asString());
    }

    @Test
    public void testNoReturn() {
        DeliaSession session = initSession(DBType.MEM);

        log("now querysvc");
        DeliaRunner deliaRunner = new DeliaRunnerImpl(session, true);
        ScalarValueBuilder valueBuilder = deliaRunner.createValueBuilder();

        String src = "let x = Person[7]";
        session.runInTransactionVoid(() -> {
            continueExecution(src);
        });
        DValue dval = session.getFinalResult().getAsDValue();
        assertEquals("bob", dval.asStruct().getField("firstName").asString());
    }

    @Test
    public void testExecute() {
        executeInTransaction = true;
        DeliaSession session = initSession(DBType.MEM);
        executeInTransaction = false;

        String src = "let x = Person[7]";
        continueExecution(src);
        DValue dval = session.getFinalResult().getAsDValue();
        assertEquals("bob", dval.asStruct().getField("firstName").asString());
    }


    //---
    protected boolean executeInTransaction = false;


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

    private DeliaSession initSession(DBType dbType) {
        DeliaRunnerImpl deliaRunner = createRunner(dbType);
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

    protected DeliaSessionImpl continueExecution(String src) {
        log.log("src: %s", src);
        delia.getOptions().executeInTransaction = executeInTransaction;
        ResultValue res = delia.continueExecution(src, sess);

        DeliaSessionImpl sessimpl = (DeliaSessionImpl) sess;
        return sessimpl;
    }

}
