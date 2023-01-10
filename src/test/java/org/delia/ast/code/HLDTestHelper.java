package org.delia.ast.code;

import org.delia.DeliaOptions;
import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.HLDBuilder;
import org.delia.hld.HLDFirstPassResults;
import org.delia.hld.dat.SyntheticDatService;
import org.delia.type.DTypeRegistry;
import org.delia.type.DTypeRegistryBuilder;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.runner.ExecutableBuilder;
import org.delia.dbimpl.ExpTestHelper;

import static org.junit.Assert.assertEquals;

public class HLDTestHelper {


    public static DeliaExecutable parseIntoHLD(FactoryService factorySvc, DeliaOptions options) {
        ExpTestHelper expHelper = new ExpTestHelper(factorySvc);
        AST.DeliaScript script = expHelper.buildScript(createValueBuilder(factorySvc));
//        assertEquals(4, script.statements.size());
        return parseIntoHLD(script, factorySvc, options);
    }
    public static DeliaExecutable parseIntoHLD(AST.DeliaScript script, FactoryService factorySvc, DeliaOptions options) {
        SyntheticDatService datSvc = new SyntheticDatService();
        HLDBuilder hldBuilder = new HLDBuilder(factorySvc, datSvc, options, "public");
        HLDFirstPassResults firstPassResults = hldBuilder.buildTypesOnly(script);

        ExecutableBuilder execBuilder = new ExecutableBuilder(factorySvc, datSvc, null);
        DeliaExecutable executable = execBuilder.buildFromScript(script, firstPassResults, DBType.POSTGRES);
        ExpTestHelper.dumpExec(executable, factorySvc.getLog());
        return executable;
    }

    protected static ScalarValueBuilder createValueBuilder(FactoryService factorySvc) {
        DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
        registryBuilder.init();
        DTypeRegistry registry = registryBuilder.getRegistry();
        return new ScalarValueBuilder(factorySvc, registry);
    }
}
