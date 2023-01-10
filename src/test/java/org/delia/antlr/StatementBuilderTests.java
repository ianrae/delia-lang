package org.delia.antlr;

import org.delia.DeliaOptions;
import org.delia.compiler.Pass1Compiler;
import org.delia.lld.processor.LLDBuilder;
import org.delia.runner.ExecutionState;
import org.delia.dbimpl.PersonStatementBuilderPlugin;
import org.delia.runner.OuterRunner;
import org.delia.dbimpl.mem.MemDBInterfaceFactory;
import org.delia.compiler.impl.CompilerResults;
import org.delia.compiler.DeliaCompiler;
import org.delia.ast.*;
import org.delia.runner.DeliaStatementBuilder;
import org.delia.dbimpl.ExpTestHelper;
import org.delia.compiler.ast.AST;
import org.delia.db.DBType;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.HLDBuilder;
import org.delia.hld.HLDFirstPassResults;
import org.delia.hld.dat.SyntheticDatService;
import org.delia.runner.BasicRunnerResults;
import org.delia.runner.ExecutableBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class StatementBuilderTests extends TestBase {


    @Test
    public void test() throws IOException {
        DeliaStatementBuilder dsb = new DeliaStatementBuilder(factorySvc);
        dsb.setPlugin(new PersonStatementBuilderPlugin(factorySvc));
        CompilerResults results = dsb.compile("Person[true]");
        AST.DeliaScript script = dsb.buildScript(results);

        assertEquals(4, script.statements.size());

        SyntheticDatService datSvc = new SyntheticDatService();
        HLDBuilder hldBuilder = new HLDBuilder(factorySvc, datSvc, new DeliaOptions(), null);
        HLDFirstPassResults firstPassResults = hldBuilder.buildTypesOnly(script);

        ExecutableBuilder execBuilder = new ExecutableBuilder(factorySvc, datSvc, null);
        Pass1Compiler pass1Compiler = new Pass1Compiler(factorySvc, firstPassResults, DBType.POSTGRES, script.errorFormatter, null);
        String schema = "alpha";
        pass1Compiler.process(script, schema);

        DeliaExecutable executable = execBuilder.buildFromScript(script, firstPassResults, DBType.POSTGRES);
        dumpExec(executable);
        LLDBuilder builder = new LLDBuilder(factorySvc, datSvc, new DeliaOptions());
        builder.buildLLD(executable);

        MemDBInterfaceFactory dbInterface = new MemDBInterfaceFactory(factorySvc);
        OuterRunner runner = new OuterRunner(factorySvc, dbInterface, datSvc);
        ExecutionState execState = runner.createNewExecutionState(executable.registry);
        BasicRunnerResults res = runner.executeOnDBInterface(executable, execState, new DeliaOptions(), true);

        assertEquals(null, res.insertResultVal);
        assertEquals(1, res.qresp.dvalList.size());
        assertEquals("bob", res.qresp.getOne().asStruct().getField("firstName").asString());

    }

    @Test
    public void testDebug() throws IOException {
        chkParse("Customer[ a].addr ", "Customer", "a", "addr");
    }

    private void chkParse(String src, String typeName, String s1, String fnChain) {
        CompilerResults zoo = compileAndRun(src);
        AST.LetStatementAst letStatementAst = zoo.getLetStatementAst();
        if (letStatementAst != null) {
            assertEquals(typeName, letStatementAst.typeName);
            assertEquals(s1, letStatementAst.whereClause.strValue());
            if (fnChain != null) {
                assertEquals(fnChain, letStatementAst.fieldAndFuncs.strValue());
            }
        } else if (zoo.operandExp != null) {
            assertEquals(s1, zoo.operandExp.strValue());
        } else {
            assertEquals(s1, zoo.elem.strValue());
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

    private void dumpExec(DeliaExecutable executable) {
        ExpTestHelper.dumpExec(executable, log);
    }

}
