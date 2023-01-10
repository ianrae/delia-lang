package org.delia.rule;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.rule.rules.CompareOpRule;
import org.delia.rule.rules.IsDateOnlyRule;
import org.delia.rule.rules.IsTimeOnlyRule;
import org.delia.rule.rules.SizeofRule;
import org.delia.tok.Tok;
import org.delia.tok.TokClauseBuilder;
import org.delia.type.DType;
import org.delia.type.DTypeInternal;
import org.delia.type.DTypeRegistry;
import org.delia.type.EffectiveShape;
import org.delia.util.DeliaExceptionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RuleBuilder extends ServiceBase {
//    private static class RuleDefPair {
//        public Exp.OperandExp exp;
//        public String fieldName;
//    }

    private RuleFunctionFactory ruleFactory;
    private DTypeRegistry registry;
    private final TokClauseBuilder tokBuilder;

    public RuleBuilder(FactoryService factorySvc, DTypeRegistry registry) {
        super(factorySvc);
        this.registry = registry;
        this.ruleFactory = factorySvc.createRuleFunctionFactory();
        this.tokBuilder = new TokClauseBuilder(factorySvc);
    }

    public void addRules(DType dtype, AST.TypeAst typeStatementAst) {
        for (Exp.RuleClause ruleExp : typeStatementAst.rules) {
            Tok.RuleTok ruleTok = tokBuilder.buildRule(ruleExp);

            if (ruleTok.where instanceof Tok.OperatorTok) {
                Tok.OperatorTok foe = (Tok.OperatorTok) ruleTok.where;
                RuleOperand oper1 = createOperand(foe.op1, dtype);
                RuleOperand oper2 = createOperand(foe.op2, dtype);
                RuleGuard guard = createGuard(oper1, oper2); //new AlwaysRuleGuard();
                DateFormatService fmtSvc = this.factorySvc.getDateFormatService();
                CompareOpRule rule = new CompareOpRule(guard, oper1, foe.op, oper2, fmtSvc);
                dtype.getRawRules().add(rule);
            } else if (ruleTok.where instanceof Tok.DottedTok) {
                Tok.DottedTok rfe = (Tok.DottedTok) ruleTok.where;
                DRule rule = ruleFactory.createRule(rfe, 0, dtype);
                if (rule != null) {
                    dtype.getRawRules().add(rule);
                } else {
                    DeliaExceptionHelper.throwError("unknown-rule", "Type %s: unknown rule '%s'", dtype.getName(), ruleExp.strValue());
                }
            }
        }
    }

    private RuleGuard createGuard(RuleOperand oper1, RuleOperand oper2) {
        List<String> fields1 = oper1 == null ? null : oper1.getFieldList();
        List<String> fields2 = oper2 == null ? null : oper2.getFieldList();

        List<String> combinedL = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fields1)) {
            combinedL.addAll(fields1);
        }
        if (CollectionUtils.isNotEmpty(fields2)) {
            combinedL.addAll(fields2);
        }

        if (combinedL.isEmpty()) {
            return new AlwaysRuleGuard();
        } else {
            return new NotNullGuard(combinedL);
        }
    }

//    private RuleOperand createOperand(Exp.OperandExp op1, DType dtype) {
    private RuleOperand createOperand(Tok.OperandTok op1, DType dtype) {
        Tok.DottedTok dexp = (Tok.DottedTok) op1;
        if (dexp.chainL.size() == 1) {
            if (dexp.chainL.get(0) instanceof Tok.FieldTok) {
                Tok.FieldTok fexp = (Tok.FieldTok) dexp.chainL.get(0);
                if (dtype.isStructShape()) {
                    StructDValueRuleOperand oper = new StructDValueRuleOperand(fexp.fieldName);
                    return oper;
                } else {
                    //TODO check here that fexp.fieldName should be 'value'
                    return new DValueRuleOperand();
                }
            } else if (dexp.chainL.get(0) instanceof Tok.ValueTok) {
                Tok.ValueTok vexp = (Tok.ValueTok) dexp.chainL.get(0);
                return new ScalarRuleOperand(vexp.value.getObject()); //TODO later use dval directly
            }
        }
        DeliaExceptionHelper.throwNotImplementedError("bad rule expr", op1.toString());
        return null;
    }


    public List<SizeofRule> findSizeofRules(DType dtype) {
        return dtype.getRawRules().stream().filter(x -> x instanceof SizeofRule).map(x -> (SizeofRule)x).collect(Collectors.toList());
    }
    public List<IsDateOnlyRule> findIsDateOnlyRules(DType dtype) {
        return dtype.getRawRules().stream().filter(x -> x instanceof IsDateOnlyRule).map(x -> (IsDateOnlyRule)x).collect(Collectors.toList());
    }
    public List<IsTimeOnlyRule> findIsTimeOnlyRules(DType dtype) {
        return dtype.getRawRules().stream().filter(x -> x instanceof IsTimeOnlyRule).map(x -> (IsTimeOnlyRule)x).collect(Collectors.toList());
    }
}
