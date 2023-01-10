package org.delia.exec;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.hld.DeliaExecutable;
import org.delia.runner.*;
import org.delia.type.*;
import org.delia.valuebuilder.BlobValueBuilder;
import org.delia.varevaluator.CustomVarEvaluator;
import org.delia.varevaluator.CustomVarEvaluatorFactory;
import org.delia.varevaluator.VarEvaluator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class BlobLoaderTests extends DeliaRunnerTestBase {

    public static class MyCustomVarEvaluator extends CustomVarEvaluator {

        private final DValue valueToReturn;
        private final String hookedVarName;

        public MyCustomVarEvaluator(VarEvaluator inner, String hookedVarName, DValue valueToReturn) {
            super(inner);
            this.hookedVarName = hookedVarName;
            this.valueToReturn = valueToReturn;
        }


        @Override
        public boolean exists(String varName) {
            if (varName.equals(hookedVarName)) {
                return true;
            }
            return super.exists(varName);
        }

        @Override
        protected List<DValue> onLookup(String varName, List<DValue> list) {
            return Collections.singletonList(valueToReturn);
        }

        @Override
        protected ResultValue onLookupAsResultValue(String varName, ResultValue existingRes) {
            ResultValue res = new ResultValue();
            res.val = valueToReturn;
            res.ok = true;
//            public Shape shape;  TODO does this need to be set?
            return res;
        }
    }

    public static class MyCustomVarEvaluatorFactory implements CustomVarEvaluatorFactory {
        public DValue valueToReturn;
        public String hookedVarName;

        @Override
        public CustomVarEvaluator create(VarEvaluator inner) {
            MyCustomVarEvaluator varEvaluator = new MyCustomVarEvaluator(inner, hookedVarName, valueToReturn);
            return varEvaluator;
        }
    }

    @Test
    public void test() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        DeliaSession sess = initSession(deliaRunner);
        DTypeName dTypeName = new DTypeName("s2", "Flight");
        DType dtype = sess.getRegistry().getType(dTypeName);
        assertEquals("Flight", dtype.getName());
        assertEquals("s2", dtype.getSchema());

        //and more.. convert blob to base64 and use directly in Delia src
        String src = "  insert Flight {id: 55, name:'bob', thumbnail: '4E/QIA==' }\n";
        String src2 = " let x = Flight[true]";
        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = deliaRunner.compile(src+src2, localET);
        assertEquals(0, localET.errorCount());

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        sess = deliaRunner.execute(executable);
        ResultValue res = sess.getFinalResult();
        List<DValue> list = res.getAsDValueList();
        assertEquals(1, list.size());
        DValue inner = list.get(0).asStruct().getField("thumbnail");
        assertEquals("4E/QIA==", inner.asString());
    }

    @Test
    public void testUsingVar() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        DeliaSession sess = initSession(deliaRunner);
        DTypeName dTypeName = new DTypeName("s2", "Flight");
        DType dtype = sess.getRegistry().getType(dTypeName);
        assertEquals("Flight", dtype.getName());
        assertEquals("s2", dtype.getSchema());

        //and more.. convert blob to base64 and use directly in Delia src as a var
        String src = "  let mydata = '4E/QIA==' \n";
        String src2 = "  insert Flight {id: 55, name:'bob', thumbnail: mydata }\n";
        String src3 = " let x = Flight[true]";
        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = deliaRunner.compile(src+src2+src3, localET);
        assertEquals(0, localET.errorCount());

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        sess = deliaRunner.execute(executable);
        ResultValue res = sess.getFinalResult();
        List<DValue> list = res.getAsDValueList();
        assertEquals(1, list.size());
        DValue inner = list.get(0).asStruct().getField("thumbnail");
        assertEquals("4E/QIA==", inner.asString());
    }

    @Test
    public void testUsingVarEvaluator() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        DeliaSession sess = initSession(deliaRunner);
        DTypeName dTypeName = new DTypeName("s2", "Flight");
        DType dtype = sess.getRegistry().getType(dTypeName);
        assertEquals("Flight", dtype.getName());
        assertEquals("s2", dtype.getSchema());

        //and more.. use custom varEvaluator so can load WrappedBlob directly
        String src = "";
        String src2 = "  insert Flight {id: 55, name:'bob', thumbnail: mydata }\n";
        String src3 = " let x = Flight[true]";
        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());

        MyCustomVarEvaluatorFactory varFactory = new MyCustomVarEvaluatorFactory();
        varFactory.valueToReturn = buildBlob(sess.getDelia().getFactoryService(), sess.getRegistry());
        varFactory.hookedVarName = "mydata";
        deliaRunner.getDelia().getOptions().customVarEvaluatorFactory = varFactory;
        AST.DeliaScript script = deliaRunner.compile(src+src2+src3, localET);
        assertEquals(0, localET.errorCount());

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        sess = deliaRunner.execute(executable);
        ResultValue res = sess.getFinalResult();
        List<DValue> list = res.getAsDValueList();
        assertEquals(1, list.size());
        DValue inner = list.get(0).asStruct().getField("thumbnail");
        assertEquals("4E/QIA==", inner.asString());
    }

    private DValue buildBlob(FactoryService factorySvc, DTypeRegistry registry) {
        BlobValueBuilder blobBuilder = new BlobValueBuilder(factorySvc, registry.getType(BuiltInTypes.BLOB_SHAPE));
        blobBuilder.buildFromString("4E/QIA==");
        boolean ok = blobBuilder.finish();
        assertEquals(true, ok);
        return blobBuilder.getDValue();
    }

    //---

    @Before
    public void init() {
    }

    private DeliaSession initSession(DeliaRunnerImpl deliaRunner) {
        Delia delia = deliaRunner.getDelia();
        this.delia = delia;

        String src = "schema s2\n";
        String src2 = "type Flight struct {id int primaryKey, name string, thumbnail blob } end";

        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = deliaRunner.compile(src+src2, localET);
        assertEquals(0, localET.errorCount());

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        DeliaSession sess = deliaRunner.execute(executable);
        return sess;
    }

}
