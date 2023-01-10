package org.delia.exec;

import org.delia.Delia;
import org.delia.base.TestBase;
import org.delia.base.UnitTestLog;
import org.delia.bddnew.core.BDDConnectionProvider;
import org.delia.compiler.ast.AST;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.DBType;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.DeliaLog;
import org.delia.runner.DeliaRunner;
import org.delia.runner.DeliaRunnerImpl;
import org.delia.tok.Tok;
import org.delia.tok.TokClauseBuilder;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class DeliaRunnerTestBase extends TestBase {


    //---
    protected DeliaRunner deliaRunner1;


    protected DeliaRunnerImpl createRunner(DBType dbType) {
        DeliaLog log = new UnitTestLog();
        BDDConnectionProvider connProvider = new BDDConnectionProvider(dbType);
        DeliaRunnerImpl deliaRunnerImpl = new DeliaRunnerImpl(connProvider.getConnectionDef(), log);
        deliaRunner1 = deliaRunnerImpl;
        Delia delia = deliaRunnerImpl.getDelia();
        this.factorySvc = (FactoryServiceImpl) delia.getFactoryService();
        this.delia = delia;

        return deliaRunnerImpl;
    }

    protected AST.DeliaScript compileSrc(String src) {
        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = deliaRunner1.compile(src, localET);
        assertEquals(0, localET.errorCount());
        return script;
    }

    protected AST.TypeAst buildSrc(String src, DeliaRunner deliaRunner) {
        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = deliaRunner.compile(src, localET);
        assertEquals(0, localET.errorCount());

        AST.TypeAst typeAst = (AST.TypeAst) script.statements.get(0);
        return typeAst;
    }

    protected AST.LetStatementAst buildSrcAndLet(String src, DeliaRunner deliaRunner, int letIndex) {
        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = deliaRunner.compile(src, localET);
        assertEquals(0, localET.errorCount());

        AST.LetStatementAst letAst = (AST.LetStatementAst) script.statements.get(letIndex);
        return letAst;
    }

    protected Tok.RuleTok buildRule(String src) {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        TokClauseBuilder tokBuilder = new TokClauseBuilder(factorySvc);
        AST.TypeAst typeAst = buildSrc(src, deliaRunner); // (AST.TypeAst) script.statements.get(0);

        Tok.RuleTok ruleTok = tokBuilder.buildRule(typeAst.rules.get(0));
        return ruleTok;
    }

    protected Tok.WhereTok buildWhere(String src) {
        return buildWhere(src, 1);
    }
    protected Tok.WhereTok buildWhere(String src, int letIndex) {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        TokClauseBuilder tokBuilder = new TokClauseBuilder(factorySvc);
        AST.LetStatementAst letAst = buildSrcAndLet(src, deliaRunner, letIndex); // (AST.TypeAst) script.statements.get(0);

        Tok.WhereTok whereTok = tokBuilder.buildWhere(letAst.whereClause);
        return whereTok;
    }

    protected Tok.DottedTok buildFieldsAndFuncs(String src, int letIndex) {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        TokClauseBuilder tokBuilder = new TokClauseBuilder(factorySvc);
        AST.LetStatementAst letAst = buildSrcAndLet(src, deliaRunner, letIndex); // (AST.TypeAst) script.statements.get(0);

        Tok.DottedTok dexp = tokBuilder.buildFieldsAndFuncs(letAst.fieldAndFuncs);
        return dexp;
    }
}
