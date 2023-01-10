package org.delia.antlr;

import org.delia.compiler.ast.AST;
import org.delia.type.DValue;
import org.delia.compiler.impl.CompilerResults;
import org.delia.compiler.DeliaCompiler;
import org.delia.ast.TestBase;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CompilerTests extends TestBase {


    @Test
    public void test() throws IOException {
//        chkParse("[false] ", "false");
//        chkParse("[true] ", "true");
//        chkParse("[ 15] ", "15");
//        chkParse("[ -15] ", "-15");
//        chkParse("[-0] ", "0");
//        chkParse("[-0] ", "0");
//        chkParse("[ x < 15] ", "x < 15");
//        chkParse("[ x] ", "x");
//        chkParse("[ x.y] ", "x.y");
//        chkParse("[ f()] ", "f()");
//        chkParse("[ a.f()] ", "a.f()");
//        chkParse("[ f(1)] ", "f(1)");
//        chkParse("[ f(1,z)] ", "f(1, z)");
//        chkParse("[ !(x < 7)] ", "!(x < 7)");
//        chkParse("[55.6] ", "55.6");
//        chkParse("[-55.6] ", "-55.6");
//        chkParse("[-55.6] ", "-55.6");
//        chkParse("[0.0] ", "0.0");
//        chkParse("[-0.0] ", "-0.0");
//        chkParse("['abc'] ", "abc");
//        chkParse("[\"a and\"] ", "a and");
//        chkParse("[''] ", "");
//        chkParse("[\"\"] ", "");
        chkParse("Customer[ a] ", "Customer", "a", null);
        chkParse("Customer[x < 07] ", "Customer", "x < 7", null);
        chkParse("let x = null ", null, null, null);
        chkParse("let x boolean = null ", "boolean", null, null);
        chkParse("let x boolean = z ", "boolean", null, null);
        chkTypeParse("type Flight struct { field1 int primaryKey} end", "Flight", "field1");
    }

    @Test
    public void testDebug() throws IOException {
//        chkTypeParse("type Flight struct { field1 int primaryKey} end", "Flight", "field1");
        chkInsertParse("insert Flight {field1: 55 }", "Flight", "55");
    }

    private void chkParse(String src, String typeName, String s1, String fnChain) {
        CompilerResults zoo = compileAndRun(src);
        AST.LetStatementAst letStatementAst = zoo.getLetStatementAst();
        if (letStatementAst != null) {
            assertEquals(typeName, letStatementAst.typeName);
            if (s1 != null) {
                assertEquals(s1, letStatementAst.whereClause.strValue());
            }
            if (fnChain != null) {
                assertEquals(fnChain, letStatementAst.fieldAndFuncs.strValue());
            }
        } else if (zoo.operandExp != null) {
            assertEquals(s1, zoo.operandExp.strValue());
        } else {
            assertEquals(s1, zoo.elem.strValue());
        }
    }
    private void chkTypeParse(String src, String typeName, String s1) {
        CompilerResults zoo = compileAndRun(src);
        AST.StatementAst stmtx = zoo.getStatementAst();
        AST.TypeAst stmt = (AST.TypeAst) stmtx;
        if (stmt != null) {
            assertEquals(typeName, stmt.typeName);
            if (s1 != null) {
                AST.TypeFieldAst fieldAST = stmt.fields.get(0);
                assertEquals(1, stmt.fields.size());
                assertEquals(s1, fieldAST.fieldName);
            }
        }
    }
    private void chkInsertParse(String src, String typeName, String s1) {
        CompilerResults zoo = compileAndRun(src);
        AST.StatementAst stmtx = zoo.getStatementAst();
        AST.InsertStatementAst stmt = (AST.InsertStatementAst) stmtx;
        if (stmt != null) {
            assertEquals(typeName, stmt.typeName);
            if (s1 != null) {
                DValue dval = stmt.fields.get(0).valueExp.value;
                String s = dval == null ? null : dval.asString();
                assertEquals(s, s1);
            }
        }
    }

    private CompilerResults compileAndRun(String src) {
        DeliaCompiler compiler = new DeliaCompiler(factorySvc);
        return compiler.compile(src);
    }

    //---

    @Before
    public void init() {
        super.init();
    }


}
