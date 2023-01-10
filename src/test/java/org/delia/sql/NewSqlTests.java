package org.delia.sql;

import org.delia.DeliaSession;
import org.delia.ast.code.CustomerAddressHelper;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.dbimpl.ExpTestHelper;
import org.delia.exec.DeliaRunnerTestBase;
import org.delia.hld.DeliaExecutable;
import org.delia.lld.LLD;
import org.delia.runner.DeliaRunnerImpl;
import org.delia.type.DTypeName;
import org.delia.type.DTypeRegistry;
import org.delia.type.DTypeRegistryBuilder;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class NewSqlTests extends DeliaRunnerTestBase {

    @Test
    public void test1() {
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());
        initSession(whereClause);

        chkSql("");
        chkSqlParams(0);
    }

    @Test
    public void test2() {
        //[true]
        Exp.WhereClause whereClause = ExpTestHelper.buildPKWhereClause(createValueBuilder(), "10");
        initSession(whereClause);
        chkSql(" WHERE a.id = ?");
        chkSqlParams(1, "10");
    }

    @Test
    public void test3() {
        //[id < 10]
        Exp.WhereClause whereClause = ExpTestHelper.buildLTWhereClause(createValueBuilder(), "Person", "id", "10", null);
        initSession(whereClause);
        chkSql(" WHERE a.id < ?");
        chkSqlParams(1, "10");
    }

    //    //1:1 rel
    @Test
    public void test4() {
        //[addr < 10]
        mainTypeName = "Customer";
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Customer"), new DTypeName(null, "Address"), "addr");
        Exp.WhereClause whereClause = ExpTestHelper.buildLTWhereClause(createValueBuilder(), "Customer", "addr", "10", joinInfo);

        initSessionCustomerAddress(whereClause);
//        chkFullSql( " WHERE b.id < ?");
        chkFullSql("SELECT a.id, a.firstName FROM alpha.customer as a LEFT JOIN address as b ON a.id=b.cust WHERE b.addr < ? ORDER BY a.id");
        chkSqlParams(1, "10");
    }

    @Test
    public void test5() {
        //Address[cust < 7]
        mainTypeName = "Address";
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Address"), new DTypeName(null, "Customer"), "cust");
        Exp.WhereClause whereClause = ExpTestHelper.buildLTWhereClause(createValueBuilder(), "Address", "cust", "7", joinInfo);

        initSessionCustomerAddress(whereClause);
        chkFullSql("SELECT a.id, a.city, a.cust FROM alpha.address as a WHERE a.cust < ? ORDER BY a.id");
        chkSqlParams(1, "7");
    }

    //    //TODO * 6 Customer[addr.city = 'toronto']
//
    @Test
    public void test7() {
        //            * 7 Customer[true].fks();
        mainTypeName = "Customer";
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());

        fieldAndFuncs = new Exp.DottedExp(new Exp.FunctionExp("fks"));
        initSessionCustomerAddress(whereClause);
        chkFullSql("SELECT a.id, a.firstName, b.id FROM alpha.customer as a LEFT JOIN address as b ON a.id=b.cust");
        chkSqlParams(0);

    }

//    //    //TODO: 8

    @Test
    public void test9() {
        //            * 9 Customer[true].addr
        mainTypeName = "Customer";
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Customer"), new DTypeName(null, "Address"), "addr");
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());

        fieldAndFuncs = new Exp.DottedExp(new Exp.FieldExp("addr", joinInfo));
        initSessionCustomerAddress(whereClause);
        chkFullSql("SELECT a.id, a.city, a.cust FROM alpha.address as a LEFT JOIN customer as b ON a.cust=b.id ORDER BY a.id");
        chkSqlParams(0);
    }

    @Test
    public void test10() {
        //            * 10 Customer[true].count()
        mainTypeName = "Customer";
//        Exp.JoinInfo joinInfo = new Exp.JoinInfo("Customer", "Address", "addr");
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());

        fieldAndFuncs = new Exp.DottedExp(new Exp.FunctionExp("count"));
        initSessionCustomerAddress(whereClause);
        chkFullSql("SELECT count(*) FROM alpha.customer as a");
        chkSqlParams(0);
    }


    //---
    protected String mainTypeName = "Person";
    private DeliaExecutable executable;
    private List<DValue> sqlParams = new ArrayList<>();
    protected boolean isManyToOne = false;
    protected boolean isMMIsReversed = false;
    protected Exp.DottedExp fieldAndFuncs;

    @Before
    public void init() {
    }

    private DeliaSession initSession(Exp.WhereClause whereClause) {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        delia = deliaRunner.getDelia();

        //build AST script for types
        ScalarValueBuilder valueBuilder = deliaRunner.createValueBuilder();
        FactoryService factorySvc = delia.getFactoryService();

        ExpTestHelper expHelper = new ExpTestHelper(factorySvc);
        AST.DeliaScript script = expHelper.buildScriptStart(valueBuilder);
        AST.LetStatementAst letStmt = new AST.LetStatementAst();
        letStmt.typeName = mainTypeName;
        letStmt.whereClause = whereClause;
        letStmt.fieldAndFuncs = fieldAndFuncs;
        script.add(letStmt);

        return runIt(deliaRunner, script);
    }

    private DeliaSession runIt(DeliaRunnerImpl deliaRunner, AST.DeliaScript script) {
        deliaRunner.getDelia().getOptions().generateSqlWhenMEMDBType = true;
        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        this.executable = executable;
        DeliaSession session = deliaRunner.execute(executable);

        sess = session;
        return session;
    }

    private DeliaSession initSessionCustomerAddress(Exp.WhereClause whereClause) {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        delia = deliaRunner.getDelia();

        //build AST script for types
        ScalarValueBuilder valueBuilder = deliaRunner.createValueBuilder();
        FactoryService factorySvc = delia.getFactoryService();

//        ExpTestHelper expHelper = new ExpTestHelper(factorySvc);
        CustomerAddressHelper caHelper = new CustomerAddressHelper(factorySvc);
        AST.DeliaScript script = caHelper.buildScriptStart(valueBuilder, isManyToOne);
        AST.LetStatementAst letStmt = new AST.LetStatementAst();
        letStmt.typeName = mainTypeName;
        letStmt.whereClause = whereClause;
        letStmt.fieldAndFuncs = fieldAndFuncs;
//        letStmt.fieldAndFuncs = fieldAndFuncs;
        script.add(letStmt);

        return runIt(deliaRunner, script);
    }

    protected ScalarValueBuilder createValueBuilder() {
        DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
        registryBuilder.init();
        DTypeRegistry registry = registryBuilder.getRegistry();
        return new ScalarValueBuilder(factorySvc, registry);
    }

    private void chkSqlParams(int expectedSize, String... vals) {
        assertEquals(expectedSize, sqlParams.size());
        assertEquals(expectedSize, vals.length);

        StringJoiner sj = new StringJoiner(",");
        for (DValue dval : sqlParams) {
            sj.add(dval.asString());
        }
        log.log("sqlParams: %s", sj);
        int i = 0;
        for (String val : vals) {
            DValue dval = sqlParams.get(i++);
            String s1 = dval.asString();
            assertEquals(val, s1);
        }
    }

    private String startOfSql = "SELECT a.id, a.firstName FROM alpha.person as a";
    private String endOfSql = " ORDER BY a.id";

    private void chkSql(String expected) {
        LLD.LLSelect stmt = (LLD.LLSelect) executable.lldStatements.get(3);
        String sql = stmt.getSql().sql;
        String s = String.format("%s%s%s", startOfSql, expected, endOfSql);
        assertEquals(s, sql);
        sqlParams = stmt.getSql().paramL;
    }

    private void chkFullSql(String expected) {
        LLD.LLSelect stmt = (LLD.LLSelect) executable.lldStatements.stream().filter(x -> x instanceof LLD.LLSelect).findAny().get();
        String sql = stmt.getSql().sql;
        assertEquals(expected, sql);
        sqlParams = stmt.getSql().paramL;
    }
}
