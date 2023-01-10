package org.delia.ast;

import org.delia.compiler.impl.CompilerResults;
import org.delia.compiler.DeliaCompiler;
import org.delia.dbimpl.ExpTestHelper;
import org.delia.compiler.ast.AST;
import org.delia.hld.HLD;
import org.delia.valuebuilder.ScalarValueBuilder;
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
public class BasicParserTests extends HLDBasicTestBase {


    @Test
    public void test1() {
        //[true]
//        DeliaAntlrTests.DeliaFilterCompiler compiler = new DeliaAntlrTests.DeliaFilterCompiler(factorySvc);
//        DeliaAntlrTests.FilterCompileResults compilerResults = compiler.compile("[true]");
        DeliaCompiler compiler = new DeliaCompiler(factorySvc);
        CompilerResults compilerResults = compiler.compile("Person[true]");

        AST.LetStatementAst letStmt = compilerResults.getLetStatementAst();
        HLD.LetHLDStatement hld = buildAndRunPersonFromLet(letStmt);
        chkLetStmt(hld, "id", "firstName");
        chkPK(hld, "id");
        chkJoins(hld, 0);
        chkWhere(hld, "[true]");
        chkAllThreeTypesSame(hld);
    }


    //-----
    @Before
    public void init() {
        super.init();
    }

    protected HLD.LetHLDStatement buildAndRunPersonFromLet(AST.LetStatementAst letStmt) {
        ScalarValueBuilder valueBuilder = createValueBuilder();
        ExpTestHelper expHelper = new ExpTestHelper(factorySvc);
        AST.DeliaScript script = expHelper.buildScriptStart(valueBuilder);
//        AST.LetStatementAst letStmt = new AST.LetStatementAst();
//        letStmt.typeName = mainTypeName;
//        letStmt.whereClause = whereClause;
        script.add(letStmt);
        return buildAndRunPerson(script, 4);
    }


}
