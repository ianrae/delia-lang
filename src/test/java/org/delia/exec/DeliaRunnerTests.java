package org.delia.exec;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.compiler.ast.AST;
import org.delia.db.DBType;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.hld.DeliaExecutable;
import org.delia.runner.DeliaRunnerImpl;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class DeliaRunnerTests extends DeliaRunnerTestBase {


    @Test
    public void testCompile() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        Delia delia = deliaRunner.getDelia();
        this.delia = delia;

        String src = "let x = 5";

        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = deliaRunner.compile(src, localET);
        assertEquals(0, localET.errorCount());

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        DeliaSession sess = deliaRunner.execute(executable);
        ResultValue res = sess.getExecutionContext().varMap.get("x");
        DValue dval = res.getAsDValue();
        assertEquals(5, dval.asInt());
    }

    @Test
    public void testCompileDollarDollar() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        Delia delia = deliaRunner.getDelia();
        this.delia = delia;

        String src = "let $$ int = 5";

        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = deliaRunner.compile(src, localET);
        assertEquals(0, localET.errorCount());

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        DeliaSession sess = deliaRunner.execute(executable);
        ResultValue res = sess.getExecutionContext().varMap.get("$$");
        DValue dval = res.getAsDValue();
        assertEquals(5, dval.asInt());
    }

    //---

    @Before
    public void init() {
    }

}
