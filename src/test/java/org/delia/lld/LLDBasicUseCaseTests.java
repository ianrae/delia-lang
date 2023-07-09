package org.delia.lld;

import org.delia.compiler.ast.Exp;
import org.delia.dbimpl.ExpTestHelper;
import org.delia.type.DTypeName;
import org.junit.Before;
import org.junit.Test;

/**
 * 1 Customer[true]
 * 2 Customer[10]
 * 3 Customer[id < 10]
 * 4 Customer[addr == 101]
 * 5 Address[cust == 7]
 * 6 Customer[addr.city = 'toronto']
 * 7 Customer[true].fks();
 * 8 Customer[true].fetch('addr')
 * 9 Customer[true].addr
 * 10 Customer[true].count()
 */
public class LLDBasicUseCaseTests extends LLDBasicTestBase {


    @Test
    public void test1() {
        //[true]
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());
        LLD.LLSelect lld = buildAndRun(whereClause);
        chkLetStmt(lld, "id", "firstName");
        chkPK(lld, "id");
        chkJoins(lld, 0);
        chkWhere(lld, "[true]");
        chkSql(lld, "SELECT a.id, a.firstName FROM alpha.person as a");
    }

    @Test
    public void test2() {
        //[true]
        Exp.WhereClause whereClause = ExpTestHelper.buildPKWhereClause(createValueBuilder(), "10");
        LLD.LLSelect lld = buildAndRun(whereClause);
        chkLetStmt(lld, "id", "firstName");
        chkPK(lld, "id");
        chkJoins(lld, 0);
        chkWhere(lld, "[10]");
        chkSql(lld, "SELECT a.id, a.firstName FROM alpha.person as a WHERE a.id = ?", "10");
    }

    @Test
    public void test3() {
        //[id < 10]
        Exp.WhereClause whereClause = ExpTestHelper.buildLTWhereClause(createValueBuilder(), "Person", "id", "10", null);
        LLD.LLSelect lld = buildAndRun(whereClause);
        chkLetStmt(lld, "id", "firstName");
        chkPK(lld, "id");
        chkJoins(lld, 0);
        chkWhere(lld, "[a.id < 10]"); //rewrite!
        chkSql(lld, "SELECT a.id, a.firstName FROM alpha.person as a WHERE a.id < ?", "10");
    }

    //    //1:1 rel
    @Test
    public void test4() {
        //[addr < 10]
        mainTypeName = "Customer";
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Customer"), new DTypeName(null, "Address"), "addr");
        Exp.WhereClause whereClause = ExpTestHelper.buildLTWhereClause(createValueBuilder(), "Customer", "addr", "10", joinInfo);

        LLD.LLSelect lld = buildAndRunCustomerAddress(whereClause);
        chkLetStmt(lld, "id", "firstName");
        chkPK(lld, "id");
        chkJoins(lld, 1);
        chkOneJoin(lld, "Customer.id.Address.cust", 0);
        chkWhere(lld, "[b.id < 10]"); //has been rewritten!
        chkSql(lld, "SELECT a.id, a.firstName FROM alpha.customer as a LEFT JOIN address as b ON a.id=b.cust WHERE b.id < ?", "10");
    }

    @Test
    public void test5() {
        //Address[cust < 7]
        mainTypeName = "Address";
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Address"), new DTypeName(null, "Customer"), "cust");
        Exp.WhereClause whereClause = ExpTestHelper.buildLTWhereClause(createValueBuilder(), "Address", "cust", "7", joinInfo);

        LLD.LLSelect lld = buildAndRunCustomerAddress(whereClause);
        chkLetStmt(lld, "id", "city", "cust");
        chkPK(lld, "id");
        chkJoins(lld, 0);
        chkWhere(lld, "[a.cust < 7]");
        chkSql(lld, "SELECT a.id, a.city, a.cust FROM alpha.address as a WHERE a.cust < ?", "7");
    }

    //TODO * 6 Customer[addr.city = 'toronto']

    @Test
    public void test7() {
        //            * 7 Customer[true].fks();
        mainTypeName = "Customer";
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());

        fieldAndFuncs = new Exp.DottedExp(new Exp.FunctionExp("fks"));
        LLD.LLSelect lld = buildAndRunCustomerAddress(whereClause);
        chkLetStmt(lld, "id", "firstName", "id");
        chkPK(lld, "id", 2);
        chkJoins(lld, 1);
        chkOneJoin(lld, "Customer.id.Address.cust", 0, "Address.id");
        chkWhere(lld, "[true]");
        chkSql(lld, "SELECT a.id, a.firstName, b.id FROM alpha.customer as a LEFT JOIN address as b ON a.id=b.cust");
    }

    //TODO: 8

    @Test
    public void test9() {
        //            * 9 Customer[true].addr
        mainTypeName = "Customer";
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Customer"), new DTypeName(null, "Address"), "addr");
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());

        fieldAndFuncs = new Exp.DottedExp(new Exp.FieldExp("addr", joinInfo));
        LLD.LLSelect lld = buildAndRunCustomerAddress(whereClause);
        mainTypeName = "Address"; //fromType
        chkLetStmt(lld, "id", "city", "cust");
        chkPK(lld, "id");
        chkJoins(lld, 1);
        chkOneJoin(lld, "Address.cust.Customer.id", 0);
        chkWhere(lld, "[true]");
        chkSql(lld, "SELECT a.id, a.city, a.cust FROM alpha.address as a LEFT JOIN customer as b ON a.cust=b.id");
    }

    @Test
    public void test10() {
        //            * 10 Customer[true].count()
        mainTypeName = "Customer";
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());

        fieldAndFuncs = new Exp.DottedExp(new Exp.FunctionExp("count"));
        LLD.LLSelect lld = buildAndRunCustomerAddress(whereClause);
        chkLetStmt(lld, "count");
        chkJoins(lld, 0);
        chkWhere(lld, "[true]");
        chkSql(lld, "SELECT count(*) FROM alpha.customer as a");
    }

    //---

    @Before
    public void init() {
        super.init();
    }

}
