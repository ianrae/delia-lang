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
 *
 * How interesting. At HLD level MM is the same!
 */
public class BasicReversedMMUseCaseTests extends HLDBasicTestBase {

    //don't need 1,2,3

    @Test
    public void test4() {
        //[addr < 10]
        mainTypeName = "Customer";
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Customer"), new DTypeName(null, "Address"), "addr");
        Exp.WhereClause whereClause = ExpTestHelper.buildLTWhereClause(createValueBuilder(), "Customer", "addr", "10", joinInfo);

        HLD.LetHLDStatement hld = buildAndRunMMCustomerAddress(whereClause);
        chkLetStmt(hld, "id", "firstName", "addr");
        chkPK(hld, "id");
        chkJoins(hld, 1);
        chkOneJoin(hld, "alpha.Customer.addr.alpha.Address", 0);
        chkWhere(hld, "[b.rightv < 10]");
        chkAllThreeTypesSame(hld);
        HLD.HLDField field = (HLD.HLDField) hld.fields.get(2);
        assertEquals(1, field.relinfo.getDatId().intValue());
    }

    @Test
    public void test5() {
        //Address[cust == 7]
        mainTypeName = "Address";
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Address"), new DTypeName(null, "Customer"), "cust");
        Exp.WhereClause whereClause = ExpTestHelper.buildLTWhereClause(createValueBuilder(), "Address", "cust", "7", joinInfo);

        HLD.LetHLDStatement hld = buildAndRunMMCustomerAddress(whereClause);
        chkLetStmt(hld, "id", "city", "cust");
        chkPK(hld, "id");
        chkJoins(hld, 1);
        chkOneJoin(hld, "alpha.Address.cust.alpha.Customer", 0);
        chkWhere(hld, "[b.leftv < 7]");
        chkAllThreeTypesSame(hld);
        HLD.HLDField field = (HLD.HLDField) hld.fields.get(2);
        assertEquals(1, field.relinfo.getDatId().intValue());
    }

    //TODO * 6 Customer[addr.city = 'toronto']

    @Test
    public void test7() {
        //            * 7 Customer[true].fks();
        mainTypeName = "Customer";
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());

        fieldAndFuncs = new Exp.DottedExp(new Exp.FunctionExp("fks"));
        HLD.LetHLDStatement hld = buildAndRunMMCustomerAddress(whereClause);
        chkLetStmt(hld, "id", "firstName", "addr");
        chkPK(hld, "id");
        chkJoins(hld, 1);
        chkOneJoin(hld, "alpha.Customer.addr.alpha.Address", 0, "alpha.Customer.addr");
        chkWhere(hld, "[true]");
        chkAllThreeTypesSame(hld);
        HLD.HLDField field = (HLD.HLDField) hld.fields.get(2);
        assertEquals(1, field.relinfo.getDatId().intValue());
    }

    //TODO: 8

    @Test
    public void test9() {
        //            * 9 Customer[true].addr
        mainTypeName = "Customer";
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Customer"), new DTypeName(null, "Address"), "addr");
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());

        fieldAndFuncs = new Exp.DottedExp(new Exp.FieldExp("addr", joinInfo));
        HLD.LetHLDStatement hld = buildAndRunMMCustomerAddress(whereClause);
        assertEquals("Customer", hld.finalField.hldTable.getName());
        assertEquals("addr", hld.finalField.pair.name);
        chkLetStmt(hld, "id", "city", "cust");
        chkPK(hld, "id");
        chkJoins(hld, 1);
        chkOneJoin(hld, "alpha.Customer.addr.alpha.Address", 0);
        chkWhere(hld, "[true]");
        chkAllThreeTypes(hld, "Address", "Address");
        HLD.HLDField field = (HLD.HLDField) hld.fields.get(2);
        assertEquals(1, field.relinfo.getDatId().intValue());
    }

    @Test
    public void test10() {
        //            * 10 Customer[true].count()
        mainTypeName = "Customer";
        Exp.WhereClause whereClause = ExpTestHelper.buildTrueWhereClause(createValueBuilder());

        fieldAndFuncs = new Exp.DottedExp(new Exp.FunctionExp("count"));
        HLD.LetHLDStatement hld = buildAndRunMMCustomerAddress(whereClause);
        assertEquals(null, hld.finalField);
        chkLetStmt(hld, "count");
        chkJoins(hld, 0);
        chkWhere(hld, "[true]");
        chkAllThreeTypes(hld, "Customer", "INTEGER_SHAPE");
    }

    //-----
    @Before
    public void init() {
        isMMIsReversed = true;
        super.init();
    }

}
