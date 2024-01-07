package org.delia.mysql;

import org.delia.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.base.DbTableCleaner;
import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.dbimpl.ExpTestHelper;
import org.delia.exec.DeliaRunnerTestBase;
import org.delia.hld.DeliaExecutable;
import org.delia.migration.MigrationAction;
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
public class MySqlBasicTests extends DeliaRunnerTestBase {


    @Test
    public void test() {
        DeliaSession session = initSession();

        log("now transaction..");
//        DeliaRunner deliaRunner = new DeliaRunnerImpl(session, true);
//        ScalarValueBuilder valueBuilder = deliaRunner.createValueBuilder();

        String src = "let x = Person[7]";
        DeliaSession tmp = session.runInTransaction(() -> {
            return continueExecution(src);
        });
        DValue dval = session.getFinalResult().getAsDValue();
        assertEquals("bob", dval.asStruct().getField("firstName").asString());
    }



    //---


    @Before
    public void init() {
//        DbTableCleaner cleaner = new DbTableCleaner();
//        cleaner.cleanDB(DBType.MYSQL);
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
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MYSQL);
        delia = deliaRunner.getDelia();

        //build AST script for types
        ScalarValueBuilder valueBuilder = deliaRunner.createValueBuilder();
        FactoryService factorySvc = delia.getFactoryService();

        ExpTestHelper expHelper = new ExpTestHelper(factorySvc);
        AST.DeliaScript script = expHelper.buildScriptStart(valueBuilder);
        //remove insert if NONE
        if (MigrationAction.NONE.equals(delia.getOptions().migrationAction)) {
            int n = script.statements.size();
            script.statements.remove(n - 1);
        }

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        DeliaSession session = deliaRunner.execute(executable);

        sess = session;
        factorySvc = delia.getFactoryService();
        return session;
    }

    protected DeliaSessionImpl continueExecution(String src) {
        log.log("src: %s", src);
        ResultValue res = delia.continueExecution(src, sess);

        DeliaSessionImpl sessimpl = (DeliaSessionImpl) sess;
        return sessimpl;
    }

}
