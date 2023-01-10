package org.delia.dbimpl.mem;

import org.delia.ConnectionDefinitionBuilder;
import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.ast.TestBase;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.error.DeliaError;
import org.delia.runner.ResultValue;
import org.delia.type.*;
import org.delia.valuebuilder.BooleanValueBuilder;
import org.delia.dbimpl.PersonStatementBuilderPlugin;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BasicMEMDeliaTests extends TestBase {

    @Test
    public void test() {
        autoAddPersonType = true;
        String src = "Person[true]";
        DeliaSession sess = runDelia(src);
        ResultValue res = sess.getExecutionContext().varMap.get("$$");
        chkDeliaResOK(res);
        assertEquals(null, res.shape);
        chkDeliaResType(res, "Person");
    }

    @Test
    public void testVar() {
        autoAddPersonType = true;
        String src = "let x = Person[true]";
        DeliaSession sess = runDelia(src);
//        String src = "Person[true]";
        ResultValue res = sess.getExecutionContext().varMap.get("$$");
        chkDeliaResOK(res);
        assertEquals(null, res.shape);
        chkDeliaResType(res, "Person");
    }

    @Test
    public void test5() {
        String src = "let x boolean = false";
        DeliaSession sess = runDelia(src);
        ResultValue res = sess.getExecutionContext().varMap.get("x");
        chkDeliaResOK(res);
        assertEquals(Shape.BOOLEAN, res.shape);
        chkDeliaResBoolVal(res, false);
    }
    @Test
    public void test5Fail() {
        String src = "let x boolean = 17";
        DeliaSession sess = runDelia(src, false);
        chkFail(sess, 1, "incompatible-assignment");
    }

    @Test
    public void testSyntaxFail() {
        String src = "let x boolean 17"; //missing =
        DeliaSession sess = runDelia(src, false);
        chkFail(sess, 1, "parse-error");
    }
    @Test
    public void testVarCopy() {
        injectBoolValue("z", true);
        String src = "let x boolean = z";
        DeliaSession sess = runDelia(src);
        ResultValue res = sess.getExecutionContext().varMap.get("$$");
        chkDeliaResOK(res);
        assertEquals(Shape.BOOLEAN, res.shape);
        chkDeliaResType(res, "BOOLEAN_SHAPE");
        assertEquals(true, res.getAsDValue().asBoolean());
    }

    @Test
    public void testVarCopyTwoStatements() {
        String src = "let z boolean = true\nlet x boolean = z";
        DeliaSession sess = runDelia(src);
        ResultValue res = sess.getExecutionContext().varMap.get("$$");
        chkDeliaResOK(res);
        assertEquals(Shape.BOOLEAN, res.shape);
        chkDeliaResType(res, "BOOLEAN_SHAPE");
        assertEquals(true, res.getAsDValue().asBoolean());
    }

    @Test
    public void testDelete() {
        autoAddPersonType = true;
        String src = "delete Person[true]";
        DeliaSession sess = runDelia(src);
        ResultValue res = sess.getExecutionContext().varMap.get("$$");
        chkDeliaResOK(res);
        assertEquals(null, res.shape);
    }

    @Test
    public void testUpdate() {
        autoAddPersonType = true;
        String src = "update Person[true] {name: 'Amy Gonzalez'}";
        DeliaSession sess = runDelia(src);
        ResultValue res = sess.getExecutionContext().varMap.get("$$");
        chkDeliaResOK(res);
        assertEquals(null, res.shape);
    }

    @Test
    public void testRule() {
        autoAddPersonType = true;
        String src = "type Customer struct {id int primaryKey, wid int } wid.maxlen(4) end";
        DeliaSession sess = runDelia(src);
        ResultValue res = sess.getExecutionContext().varMap.get("$$");
        chkDeliaResOK(res);
        assertEquals(null, res.shape);
    }

    //---
    private String dvalToInjectName = null;
    private DValue dvalToInject = null;
    private boolean autoAddPersonType = false;

    @Before
    public void init() {
        super.init();
    }

//    @After
//    public void shutdown() {
//        autoAddPersonType = false;
//    }

    protected void chkDeliaResFail(ResultValue res, String errId) {
        assertEquals(false, res.ok);
        DeliaError err = res.getLastError();
        assertEquals(true, err.getId().equals(errId));
    }

    protected void chkDeliaResOK(ResultValue res) {
        assertEquals(true, res.ok);
        assertEquals(true, res.errors.isEmpty());
    }

    protected void chkDeliaResBoolVal(ResultValue res, boolean expected) {
        DValue dval = res.getAsDValue();
        assertEquals(expected, dval.asBoolean());
    }

    protected void chkDeliaResType(ResultValue res, String typeName) {
        DValue dval = res.getAsDValue();
        assertEquals(typeName, dval.getType().getName());
    }
    private DeliaSession runDelia(String src) {
        return runDelia(src, true);
    }
    private DeliaSession runDelia(String src, boolean expected) {
        ConnectionDefinition connDef = ConnectionDefinitionBuilder.createMEM();
        DeliaGenericDao dao = new DeliaGenericDao(connDef, log);

        Delia delia = dao.getDelia();
        if (dvalToInject != null) {
            delia.injectVar(dvalToInjectName, dvalToInject);
        }
        if (autoAddPersonType) {
            delia.getOptions().statementBuilderPlugin = new PersonStatementBuilderPlugin(delia.getFactoryService());
        }

        log.log("deliaSrc: %s", src);
        boolean b = dao.initialize(src); //TODO we need to actually parse src to AST
        assertEquals(expected, b);

        DeliaSession sess = dao.getMostRecentSession();
        return sess;
    }
    private void chkErrors(ResultValue res, int numExpected, String expectedId) {
        res.errors.forEach(err -> log.log("err: %s", err.toString()));
        assertEquals(numExpected, res.errors.size());
        String msg = res.errors.get(0).getId();
        assertEquals(expectedId, msg);
    }
    private void chkFail(DeliaSession sess, int numExpected, String expectedId) {
        assertEquals(false, sess.ok());
        ResultValue res = sess.getFinalResult();
        assertEquals(false, res.ok);
        chkErrors(res, numExpected, expectedId);
    }

    private DValue injectBoolValue(String varName, boolean b) {
        DTypeRegistry registry = createSimpleRegistry();
        BooleanValueBuilder builder = new BooleanValueBuilder(registry.getType(BuiltInTypes.BOOLEAN_SHAPE));
        builder.buildFrom(b);
        DValue dval =  builder.getDValue();
        dvalToInjectName = varName;
        dvalToInject = dval;
        return dval;
    }

    private DTypeRegistry createSimpleRegistry() {
        DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
        registryBuilder.init();
        DTypeRegistry registry = registryBuilder.getRegistry();
        return registry;
    }

}
