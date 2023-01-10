package org.delia.ast;

import org.delia.compiler.ast.Exp;
import org.delia.dbimpl.ExpTestHelper;
import org.delia.hld.HLD;
import org.delia.type.DTypeName;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class DatTests extends HLDBasicTestBase {


    @Test
    public void test1() {
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


    //---

    @Before
    public void init() {
        super.init();
    }

}
