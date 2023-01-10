package org.delia.exec;

import org.delia.tok.Tok;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class TokBuilderTests extends DeliaRunnerTestBase {


    @Test
    public void testOp() {
        String src = "type Flight struct {id int primaryKey, name string } wid < 10 end";
        Tok.RuleTok ruleTok = buildRule(src);
        assertEquals("wid < 10", ruleTok.strValue());
    }

    @Test
    public void testOpFunc() {
        String src = "type Flight struct {id int primaryKey, name string } wid.len() < 10 end";
        Tok.RuleTok ruleTok = buildRule(src);
        assertEquals("wid < 10", ruleTok.strValue());
        assertEquals("wid.len() < 10", ruleTok.toString());
    }

    @Test
    public void testField() {
        String src = "type Flight struct {id int primaryKey, name string } name.maxlen(4) end";
        Tok.RuleTok ruleTok = buildRule(src);
        assertEquals("name", ruleTok.strValue());
    }

    @Test
    public void testField2() {
        String src = "type Flight struct {id int primaryKey, name string } name.f1.maxlen(4) end";
        Tok.RuleTok ruleTok = buildRule(src);
        assertEquals("f1,name", ruleTok.strValue());
    }


    @Test
    public void testField3() {
        String src = "type Flight struct {id int primaryKey, field1 int, field2 int } uniqueFields(field1,field2) end";
        Tok.RuleTok ruleTok = buildRule(src);
        assertEquals("uniqueFields(field1, field2)", ruleTok.strValue());
    }

    @Test
    public void testScalar() {
        String src = "type Grade string contains('bb') end";
        Tok.RuleTok ruleTok = buildRule(src);
        assertEquals("contains(bb)", ruleTok.strValue());
    }

    @Test
    public void testScalar2() {
        String src = "type Grade string contains('bb',6) end";
        Tok.RuleTok ruleTok = buildRule(src);
        assertEquals("contains(bb, 6)", ruleTok.strValue());
    }

    @Test
    public void testWherePK() {
        String src = "type Flight struct {id int primaryKey, name string } end";
        String src2 = "\nlet x = Flight[55]";
        Tok.WhereTok whereTok = buildWhere(src + src2);
        assertEquals("55", whereTok.strValue());
    }

    @Test
    public void testWhereOp() {
        String src = "type Flight struct {id int primaryKey, name string } end";
        String src2 = "\nlet x = Flight[id > 10]";
        Tok.WhereTok whereTok = buildWhere(src + src2);
        assertEquals("id > 10", whereTok.strValue());
    }

    @Test
    public void testWhereOpAnd() {
        String src = "type Flight struct {id int primaryKey, wid int optional, h int optional }  end";
        String src2 = "\n let x = Flight[wid < 1 and h < 100]";
        Tok.WhereTok whereTok = buildWhere(src + src2);
        assertEquals("wid < 1 and h < 100", whereTok.strValue());
    }

    @Test
    public void testWhereIn() {
        String src = "type Flight struct {id int primaryKey, wid int optional, h int optional }  end";
        String src2 = "\n let x = Flight[id in [55,56]]";
        Tok.WhereTok whereTok = buildWhere(src + src2);
        assertEquals("id in 55,56", whereTok.strValue());
    }

    @Test
    public void testFieldsAndFuncs() {
        String src = "type Flight struct {id int primaryKey, wid int optional, h int optional }  end";
        String src2 = "\n let x = Flight[55].max()";
        Tok.DottedTok dexp = buildFieldsAndFuncs(src + src2, 1);
        assertEquals("max()", dexp.strValue());
    }

    @Test
    public void testFieldsAndFuncs2() {
        String src = " type Customer struct {id int primaryKey, relation addr Address one } end\n" +
                " type Address struct {id int primaryKey, relation cust Customer optional many } end\n";
        String src2 = "\n let x = Customer[55].addr.fks().id.max()";
        Tok.DottedTok dexp = buildFieldsAndFuncs(src + src2, 2);
        assertEquals("addr,id", dexp.strValue());
        assertEquals("addr.fks(),id.max()", dexp.toString());
    }

    @Test
    public void testFieldsAndFuncs3() {
        String src = " type Customer struct {id int primaryKey, relation addr Address one } end\n" +
                " type Address struct {id int primaryKey, relation cust Customer optional many } end\n";
        String src2 = "\n let x = Customer[55].addr";
        Tok.DottedTok dexp = buildFieldsAndFuncs(src + src2, 2);
        assertEquals("addr", dexp.strValue());
        assertEquals("addr", dexp.toString());
    }

    @Test
    public void testFieldsAndFuncs3a() {
        String src = " type Customer struct {id int primaryKey, relation addr Address one } end\n" +
                " type Address struct {id int primaryKey, relation cust Customer optional many } end\n";
        String src2 = "\n let x = Customer[55].addr.id";
        Tok.DottedTok dexp = buildFieldsAndFuncs(src + src2, 2);
        assertEquals("addr,id", dexp.strValue());
        assertEquals("addr,id", dexp.toString());
    }

    //---

    @Before
    public void init() {
    }

}
