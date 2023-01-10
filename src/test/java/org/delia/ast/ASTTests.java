package org.delia.ast;

import org.delia.DeliaOptions;
import org.delia.compiler.Pass1Compiler;
import org.delia.compiler.ast.AST;
import org.delia.db.DBType;
import org.delia.dbimpl.ExpTestHelper;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.HLDBuilder;
import org.delia.hld.HLDFirstPassResults;
import org.delia.hld.dat.SyntheticDatService;
import org.delia.runner.ExecutableBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Antlr lets us create our own AST classes, rather than JParsec that dictated the ASTs
 * <p>
 * So
 * antlr parser -> listener -> these ASTs
 * script is list of statement ASTs
 * * compiler pass 2,3,4
 * create dbinterface :MEM,H2,..
 * runner executes the AST statements
 * -can update the runner exec context (eg let x = 5)
 * -or can do SQL
 * -first pass HLD generates the registry
 * -each statement generate HLD
 * -at HLD level an assoc table is magic. as if were infinite numbers of leftv and rightv. so always one magic row
 * -HERE is where we can optimize and merge multiple HLDs into single statement
 * -then if MEM ---> exec HLD directly (because we don't want MEM to half to parse sql!)
 * -else ---> each HLD obj..convert to LLD, produce a list of LLD statements
 * -convert magic assoc table to list of DELETE and UPSERT statements
 * -HERE is also where we can optimize to produce better LLD
 * -convert LLD statements to sql
 * -dbinterface exec the SQL
 * <p>
 * This was my fundamental mistake with first delia. i build MEM first
 * <p>
 * <p>
 * TODO
 * drop Long. only have Integer
 */
public class ASTTests extends TestBase {


    @Test
    public void test() {
        assertEquals(1, 1);
        AST.DeliaScript script = buildScript();
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
    }

    private void dumpExec(DeliaExecutable executable) {
        ExpTestHelper.dumpExec(executable, log);
    }

    //---

    @Before
    public void init() {
        super.init();
    }

    private AST.DeliaScript buildScript() {
        ExpTestHelper expHelper = new ExpTestHelper(factorySvc);
        return expHelper.buildScript(createValueBuilder());
    }

}
