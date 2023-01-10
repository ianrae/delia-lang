package org.delia.hld;

import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.rule.AlwaysRuleGuard;
import org.delia.rule.RuleGuard;
import org.delia.rule.StructDValueRuleOperand;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;

public class NewRelationRuleBuilder extends ServiceBase {
    private static class RuleDefPair {
        //        public XNAFSingleExp exp;
        public String fieldName;
    }

    private DTypeRegistry registry;
//    private RuleFunctionFactory ruleFactory;

    public NewRelationRuleBuilder(FactoryService factorySvc, DTypeRegistry registry) {
        super(factorySvc);
        this.registry = registry;
//        this.ruleFactory = factorySvc.createRuleFunctionFactory();
    }

    public void addRelationRules(DStructType dtype, AST.TypeAst typeStatementExp) {
        for (TypePair pair : dtype.getAllFields()) {
            DType possibleStruct = pair.type;
            if (possibleStruct == null) {
                continue;
            }
            if (possibleStruct.isStructShape()) {
                AST.TypeFieldAst fieldExp = getFieldExp(typeStatementExp, pair.name);
                if (fieldExp.isOne) {
                    //NOTE: why was fieldExp.isPrimaryKey || here??
                    StructDValueRuleOperand oper = new StructDValueRuleOperand(pair.name);
                    RuleGuard guard = new AlwaysRuleGuard();
                    String relName = getRelationName(fieldExp, pair.name);
                    RelationOneRule rule = new RelationOneRule(guard, oper, dtype, registry, fieldExp.isParent, relName);
                    rule.nameIsExplicit = fieldExp.relationName != null;
                    dtype.getRawRules().add(rule);
                } else if (fieldExp.isMany) {
                    StructDValueRuleOperand oper = new StructDValueRuleOperand(pair.name);
                    RuleGuard guard = new AlwaysRuleGuard();
                    String relName = getRelationName(fieldExp, pair.name);
                    RelationManyRule rule = new RelationManyRule(guard, oper, dtype, registry, relName);
                    rule.nameIsExplicit = fieldExp.relationName != null;
                    dtype.getRawRules().add(rule);
                }
            }
        }
    }

    private String getRelationName(AST.TypeFieldAst fieldExp, String fieldName) {
        if (fieldExp.relationName != null) {
            return fieldExp.relationName;
        }
//		String name = String.format("__rule%d", factorySvc.getNextGeneratedRuleId());
        return null; //fieldName;
    }

    private AST.TypeFieldAst getFieldExp(AST.TypeAst typeStatementExp, String fieldName) {
        for (AST.TypeFieldAst fieldExp : typeStatementExp.fields) {
            if (fieldExp.fieldName.equals(fieldName)) {
                return fieldExp;
            }
        }
        return null;
    }

}
