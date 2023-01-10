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
import org.delia.type.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class SchemaTests extends DeliaRunnerTestBase {


    @Test
    public void test() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        Delia delia = deliaRunner.getDelia();
        this.delia = delia;

        String src = "schema s2\n";
        String src2 = "type Flight struct {id int primaryKey, name string } end";

        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = deliaRunner.compile(src+src2, localET);
        assertEquals(0, localET.errorCount());

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        DeliaSession sess = deliaRunner.execute(executable);
        DTypeName dTypeName = new DTypeName("s2", "Flight");
        DType dtype = sess.getRegistry().getType(dTypeName);
        assertEquals("Flight", dtype.getName());
        assertEquals("s2", dtype.getSchema());
    }

    @Test
    public void testBase() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        Delia delia = deliaRunner.getDelia();
        this.delia = delia;

        String src = "type Flight struct {id int primaryKey, name string } end\n";
        String src2 = "schema s2\n";
        String src3 = "type Grade int end\n";
        String src4 = "type Flight2 Flight {wid Grade } end\n";

        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = deliaRunner.compile(src+src2+src3+src4, localET);
        assertEquals(0, localET.errorCount());

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        DeliaSession sess = deliaRunner.execute(executable);
        DTypeName dTypeName = new DTypeName(null, "Flight");
        DStructType structType = sess.getRegistry().getStructType(dTypeName);
        assertEquals("Flight", structType.getName());
        assertEquals(null, structType.getSchema());

        dTypeName = new DTypeName("s2", "Flight2");
        structType = sess.getRegistry().getStructType(dTypeName);
        assertEquals("Flight2", structType.getName());
        assertEquals("s2", structType.getSchema());

        TypePair pair = structType.findField("wid");
        assertEquals("s2", pair.type.getSchema());
        assertEquals("Grade", pair.type.getName());

    }

    //---

    @Before
    public void init() {
    }

}
