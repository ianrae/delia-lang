package org.delia.compiler;

import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.error.ErrorFormatter;
import org.delia.error.SimpleErrorTracker;
import org.delia.hld.HLDFirstPassResults;
import org.delia.rule.DRule;
import org.delia.rule.FieldExistenceServiceImpl;
import org.delia.type.DType;
import org.delia.type.DTypeName;
import org.delia.type.DTypeRegistry;

//final checks using registry
public class Pass4RuleChecker extends CompilerPassBase {
    private DTypeRegistry registry;

    public Pass4RuleChecker(FactoryService factorySvc, HLDFirstPassResults firstPassResults, DBType dbType, DTypeRegistry registry, ErrorFormatter errorFormatter) {
        super(factorySvc, firstPassResults, dbType, errorFormatter);
        this.registry = registry;
    }

    public void checkType(AST.TypeAst typeExp, CompilerPassResults results) {
        DTypeName dtypeName = new DTypeName(typeExp.schemaName, typeExp.typeName);
        DType dtype = registry.getType(dtypeName);
        for (DRule rule : dtype.getRawRules()) {
            checkRule(rule, dtype, results);
        }
    }

    private void checkRule(DRule rule, DType dtype, CompilerPassResults results) {
        FieldExistenceServiceImpl fieldExistSvc = new FieldExistenceServiceImpl(registry, dtype);
        SimpleErrorTracker et = new SimpleErrorTracker(log);
        rule.performCompilerPass4Checks(dtype, fieldExistSvc, et);
        results.errors.addAll(et.getErrors());
    }
}