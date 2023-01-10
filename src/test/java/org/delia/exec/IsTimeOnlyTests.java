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

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class IsTimeOnlyTests extends DeliaRunnerTestBase {

    @Test
    public void test() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        DeliaSession sess = initSession(deliaRunner);

        //and more.. convert blob to base64 and use directly in Delia src
        String src = "  insert Flight {id: 55, name:'bob', birthDate: '09:30' }\n";
        String src2 = " let x = Flight[true]";
        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = deliaRunner.compile(src+src2, localET);
        assertEquals(0, localET.errorCount());

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        sess = deliaRunner.execute(executable);
        ResultValue res = sess.getFinalResult();
        List<DValue> list = res.getAsDValueList();
        assertEquals(1, list.size());
        DValue inner = list.get(0).asStruct().getField("birthDate");
        assertEquals("1970-01-01T09:30:00.000+0000", inner.asString());
    }

    @Test
    public void test2() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        DeliaSession sess = initSession(deliaRunner);

        //and more.. convert blob to base64 and use directly in Delia src
        String src = "  insert Flight {id: 55, name:'bob', birthDate: '2017' }\n";
        String src2 = " let x = Flight[true]";
        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = deliaRunner.compile(src+src2, localET);
        assertEquals(0, localET.errorCount());

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        sess = deliaRunner.execute(executable);
        ResultValue res = sess.getFinalResult();
        List<DValue> list = res.getAsDValueList();
        assertEquals(1, list.size());
        DValue inner = list.get(0).asStruct().getField("birthDate");
        assertEquals("2017-01-01T00:00:00.000+0000", inner.asString());
    }



    //---

    @Before
    public void init() {
    }

    private DeliaSession initSession(DeliaRunnerImpl deliaRunner) {
        Delia delia = deliaRunner.getDelia();
        this.delia = delia;

        String src = "schema s2\n";
        String src2 = "type Flight struct {id int primaryKey, name string, birthDate date } birthDate.isTimeOnly() end";

        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = deliaRunner.compile(src+src2, localET);
        assertEquals(0, localET.errorCount());

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        DeliaSession sess = deliaRunner.execute(executable);
        return sess;
    }

}
