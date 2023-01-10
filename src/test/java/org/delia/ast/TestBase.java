package org.delia.ast;

import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.DeliaLog;
import org.delia.log.SimpleLog;
import org.delia.type.DTypeRegistry;
import org.delia.type.DTypeRegistryBuilder;
import org.delia.valuebuilder.ScalarValueBuilder;

public class TestBase {

    //---
    protected DeliaLog log;
    protected FactoryService factorySvc;

    public void init() {
        log = new SimpleLog();
        ErrorTracker et = new SimpleErrorTracker(log);
        factorySvc = new FactoryServiceImpl(log, et);
    }

    protected ScalarValueBuilder createValueBuilder() {
        DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
        registryBuilder.init();
        DTypeRegistry registry = registryBuilder.getRegistry();
        return new ScalarValueBuilder(factorySvc, registry);
    }

    protected String getSchemaIfPresent(AST.DeliaScript script) {
        if (script.statements.size() > 0) {
            AST.StatementAst stmt = script.statements.get(0);
            if (stmt instanceof AST.SchemaAst) {
                AST.SchemaAst schemaStmt = (AST.SchemaAst) stmt;
                return schemaStmt.schemaName;
            }
        }
        return null;
    }

}
