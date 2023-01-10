package org.delia.exec;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.compiler.ast.AST;
import org.delia.compiler.impl.DeliaParseException;
import org.delia.db.DBType;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.hld.DeliaExecutable;
import org.delia.runner.DeliaException;
import org.delia.runner.DeliaRunnerImpl;
import org.delia.runner.ResultValue;
import org.delia.type.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class LocTests extends DeliaRunnerTestBase {

    @Test
    public void testParseError() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        Delia delia = deliaRunner.getDelia();
        this.delia = delia;

        String src = "xschema s2\n";

        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = null;
        boolean failed = false;
        try {
            script = deliaRunner.compile(src, localET);
        } catch (DeliaParseException e) {
            failed = true;
            log.log("%d:%d %s", e.loc.lineNum, e.loc.charOffset, e.getMessage());
        }
        assertEquals(true, failed);
    }

    @Test
    public void testPass2Error() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        Delia delia = deliaRunner.getDelia();
        this.delia = delia;

        String src = "type Flight struct {id int primaryKey, wid int optional, wid string optional }  end";

        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = null;
        boolean failed = false;
        try {
            script = deliaRunner.compile(src, localET);
            DeliaExecutable executable = deliaRunner.buildExecutable(script);
        } catch (DeliaException e) {
            failed = true;
            DeliaError err = e.getFirstError();
            //log.log("%d:%d %s", err.getLoc().lineNum, err.getLoc().charOffset, e.getMessage());
        }
        assertEquals(true, failed);
    }

    @Test
    public void testHLDError() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        Delia delia = deliaRunner.getDelia();
        this.delia = delia;

        String src = "type Flight struct {id int primaryKey, wid int optional}  end\n";
        String src2 = "let x = Flight[1].widx.min()";

        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = null;
        boolean failed = false;
        try {
            script = deliaRunner.compile(src+src2, localET);
            DeliaExecutable executable = deliaRunner.buildExecutable(script);
        } catch (DeliaException e) {
            failed = true;
            DeliaError err = e.getFirstError();
//            log.log("exception: %d:%d %s", err.getLoc().lineNum, err.getLoc().charOffset, e.getMessage());
//            log.log("exception: %d:%d %s", 0, 0, e.getMessage());
            String ss = script.errorFormatter.format(err);
            log.log(ss);
        }
        assertEquals(true, failed);
    }

    @Test
    public void testLLDError() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        Delia delia = deliaRunner.getDelia();
        this.delia = delia;

        String src = "type Flight struct {id int primaryKey, wid int optional}  end\n";
        String src2 = "let x = Flight[1].widx.min()";

        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = null;
        boolean failed = false;
        try {
            script = deliaRunner.compile(src+src2, localET);
            DeliaExecutable executable = deliaRunner.buildExecutable(script);
        } catch (DeliaException e) {
            failed = true;
            DeliaError err = e.getFirstError();
//            log.log("exception: %d:%d %s", err.getLoc().lineNum, err.getLoc().charOffset, e.getMessage());
//            log.log("exception: %d:%d %s", 0, 0, e.getMessage());
            String ss = script.errorFormatter.format(err);
            log.log(ss);
        }
        assertEquals(true, failed);
    }

    //---

    @Before
    public void init() {
    }

}
