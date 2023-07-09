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
public class LLDBasicMMUseCaseTests extends LLDBasicTestBase {

    //don't need 1,2,3

    //    //1:1 rel
    @Test
    public void test4() { //SAT!!
        //[addr < 10]
        mainTypeName = "Customer";
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Customer"), new DTypeName(null, "Address"), "addr");
        Exp.WhereClause whereClause = ExpTestHelper.buildLTWhereClause(createValueBuilder(), "Customer", "addr", "10", joinInfo);

        LLD.LLSelect lld = buildAndRunMMCustomerAddress(whereClause);
        chkLetStmt(lld, "id", "firstName"); //we did't do join yet so include addr not in results
        chkPK(lld, "id");
        chkJoins(lld, 1);
        chkOneJoin(lld, "Customer.id.CustomerAddressDat1.leftv", 0);
        chkWhere(lld, "[b.rightv < 10]"); //has been rewritten!
        chkSql(lld, "SELECT a.id, a.firstName FROM alpha.customer as a LEFT JOIN customeraddressdat1 as b ON a.id=b.leftv WHERE b.rightv < ?", "10");
    }

    @Test
    public void test5() {
        //Address[cust < 7]
        mainTypeName = "Address";
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Address"), new DTypeName(null, "Customer"), "cust");
        Exp.WhereClause whereClause = ExpTestHelper.buildLTWhereClause(createValueBuilder(), "Address", "cust", "7", joinInfo);

        LLD.LLSelect lld = buildAndRunMMCustomerAddress(whereClause);
        chkLetStmt(lld, "id", "city");
        chkPK(lld, "id");
        chkJoins(lld, 1);
        chkOneJoin(lld, "Address.id.CustomerAddressDat1.rightv", 0);
        chkWhere(lld, "[b.leftv < 7]");
        chkSql(lld, "SELECT a.id, a.city FROM alpha.address as a LEFT JOIN customeraddressdat1 as b ON a.id=b.rightv WHERE b.leftv < ?", "7");
    }

    //TODO * 6 Customer[addr.city = 'toronto']

    @Test
    public void test7() {
        //            * 7 Customer[true].fks();
        mainTypeName = "Customer";
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());

        fieldAndFuncs = new Exp.DottedExp(new Exp.FunctionExp("fks"));
        LLD.LLSelect lld = buildAndRunMMCustomerAddress(whereClause);
        chkLetStmt(lld, "id", "firstName", "rightv");
        chkPK(lld, "id", 1);
        chkJoins(lld, 1);
        chkOneJoin(lld, "Customer.id.CustomerAddressDat1.leftv", 0);
        chkWhere(lld, "[true]");
        chkSql(lld, "SELECT a.id, a.firstName, b.rightv FROM alpha.customer as a LEFT JOIN customeraddressdat1 as b ON a.id=b.leftv");
    }

    //TODO: 8

    @Test
    public void test9() {
        //            * 9 Customer[true].addr
        mainTypeName = "Customer";
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Customer"), new DTypeName(null, "Address"), "addr");
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());

        fieldAndFuncs = new Exp.DottedExp(new Exp.FieldExp("addr", joinInfo));
        LLD.LLSelect lld = buildAndRunMMCustomerAddress(whereClause);
        mainTypeName = "Address"; //fromType
        chkLetStmt(lld, "id", "city"); //, "leftv");
        chkPK(lld, "id");
        chkJoins(lld, 1);
        chkOneJoin(lld, "Address.id.CustomerAddressDat1.rightv", 0);
        chkWhere(lld, "[true]");
        chkSql(lld, "SELECT a.id, a.city FROM alpha.address as a LEFT JOIN customeraddressdat1 as b ON a.id=b.rightv");
    }

    @Test
    public void test10() {
        //            * 10 Customer[true].count()
        mainTypeName = "Customer";
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());

        fieldAndFuncs = new Exp.DottedExp(new Exp.FunctionExp("count"));
        LLD.LLSelect lld = buildAndRunMMCustomerAddress(whereClause);
        chkLetStmt(lld, "count");
        chkJoins(lld, 0);
        chkWhere(lld, "[true]");
        chkSql(lld, "SELECT count(*) FROM alpha.customer as a");
    }

    //---

    @Before
    public void init() {
        super.init();
        this.isManyToOne = true;
    }

}
