package org.delia.ast;

import org.delia.compiler.ast.Exp;
import org.delia.dbimpl.ExpTestHelper;
import org.delia.hld.HLD;
import org.delia.type.DTypeName;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
public class BasicN1UseCaseTests extends org.delia.ast.HLDBasicTestBase {

    @Test
    public void test1() {
        //[true]
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());
        HLD.LetHLDStatement hld = buildAndRunPerson(whereClause);
        chkLetStmt(hld, "id", "firstName");
        chkPK(hld, "id");
        chkJoins(hld, 0);
        chkWhere(hld, "[true]");
        chkAllThreeTypesSame(hld);
    }

    @Test
    public void test2() {
        //[true]
        Exp.WhereClause whereClause = ExpTestHelper.buildPKWhereClause(createValueBuilder(), "10");
        HLD.LetHLDStatement hld = buildAndRunPerson(whereClause);
        chkLetStmt(hld, "id", "firstName");
        chkPK(hld, "id");
        chkJoins(hld, 0);
        chkWhere(hld, "[10]");
        chkAllThreeTypesSame(hld);
    }

    @Test
    public void test3() {
        //[true]
        Exp.WhereClause whereClause = ExpTestHelper.buildLTWhereClause(createValueBuilder(), "Person", "id", "10", null);
        HLD.LetHLDStatement hld = buildAndRunPerson(whereClause);
        chkLetStmt(hld, "id", "firstName");
        chkPK(hld, "id");
        chkJoins(hld, 0);
        chkWhere(hld, "[a.id < 10]");
        chkAllThreeTypesSame(hld);
    }

    @Test
    public void test4() {
        //[addr < 10]
        mainTypeName = "Customer";
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Customer"), new DTypeName(null, "Address"), "addr");
        Exp.WhereClause whereClause = ExpTestHelper.buildLTWhereClause(createValueBuilder(), "Customer", "addr", "10", joinInfo);

        HLD.LetHLDStatement hld = buildAndRunCustomerAddress(whereClause);
        chkLetStmt(hld, "id", "firstName", "addr");
        chkPK(hld, "id");
        chkJoins(hld, 1);
        chkOneJoin(hld, "alpha.Customer.addr.alpha.Address", 0);
        chkWhere(hld, "[b.id < 10]");
        chkAllThreeTypesSame(hld);
    }

    @Test
    public void test5() {
        //Address[cust == 7]
        mainTypeName = "Address";
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Address"), new DTypeName(null, "Customer"), "cust");
        Exp.WhereClause whereClause = ExpTestHelper.buildLTWhereClause(createValueBuilder(), "Address", "cust", "7", joinInfo);

        HLD.LetHLDStatement hld = buildAndRunCustomerAddress(whereClause);
        chkLetStmt(hld, "id", "city", "cust");
        chkPK(hld, "id");
        chkJoins(hld, 0);
//        chkOneJoin(hld, "Address.cust.Customer", 0);
        chkWhere(hld, "[a.cust < 7]");
        chkAllThreeTypesSame(hld);
    }

    //TODO * 6 Customer[addr.city = 'toronto']

    @Test
    public void test7() {
        //            * 7 Customer[true].fks();
        mainTypeName = "Customer";
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());

        fieldAndFuncs = new Exp.DottedExp(new Exp.FunctionExp("fks"));
        HLD.LetHLDStatement hld = buildAndRunCustomerAddress(whereClause);
        chkLetStmt(hld, "id", "firstName", "addr");
        chkPK(hld, "id");
        chkJoins(hld, 1);
        chkOneJoin(hld, "alpha.Customer.addr.alpha.Address", 0, "alpha.Customer.addr");
        chkWhere(hld, "[true]");
        chkAllThreeTypesSame(hld);
    }

    //TODO: 8

    @Test
    public void test9() {
        //            * 9 Customer[true].addr
        mainTypeName = "Customer";
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Customer"), new DTypeName(null, "Address"), "addr");
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());

        fieldAndFuncs = new Exp.DottedExp(new Exp.FieldExp("addr", joinInfo));
        HLD.LetHLDStatement hld = buildAndRunCustomerAddress(whereClause);
        assertEquals("Customer", hld.finalField.hldTable.getName());
        assertEquals("addr", hld.finalField.pair.name);
        chkLetStmt(hld, "id", "city", "cust");
        chkPK(hld, "id");
        chkJoins(hld, 1);
        chkOneJoin(hld, "alpha.Customer.addr.alpha.Address", 0);
        chkWhere(hld, "[true]");
        chkAllThreeTypes(hld, "Address", "Address");
    }

    @Test
    public void test10() {
        //            * 10 Customer[true].count()
        mainTypeName = "Customer";
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());

        fieldAndFuncs = new Exp.DottedExp(new Exp.FunctionExp("count"));
        HLD.LetHLDStatement hld = buildAndRunCustomerAddress(whereClause);
        assertEquals(null, hld.finalField);
        chkLetStmt(hld, "count");
        chkJoins(hld, 0);
        chkWhere(hld, "[true]");
        chkAllThreeTypes(hld, "Customer", "INTEGER_SHAPE");
    }

    //-----
    @Before
    public void init() {
        super.init();
        this.isManyToOne = true;
    }

}
