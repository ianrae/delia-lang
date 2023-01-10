package org.delia.rule;

import org.apache.commons.collections.CollectionUtils;
import org.delia.core.FactoryService;
import org.delia.rule.rules.*;
import org.delia.tok.Tok;
import org.delia.type.*;
import org.delia.util.DeliaExceptionHelper;

import java.util.ArrayList;
import java.util.List;

public class DefaultRuleFunctionBuilder implements RuleFunctionBulder {
    private FactoryService factorySvc;

    public DefaultRuleFunctionBuilder(FactoryService factorySvc) {
        this.factorySvc = factorySvc;
    }

    /* (non-Javadoc)
     * @see org.delia.typebuilder.RuleFunctionFactory#createRule(org.delia.compiler.astx.XNAFMultiExp, int, org.delia.type.DType)
     */
    @Override
    public DRule createRule(Tok.DottedTok rfe, int index, DType dtype) {
        Tok.DottedTok dexp = (Tok.DottedTok) rfe;
        //only one func. TODO: fix later!!

        String fieldName = null;
        Tok.FunctionTok funcTok = null;
        Tok.DToken dtok = dexp.chainL.get(0);
        if (dtok instanceof Tok.FieldTok) {
            Tok.FieldTok fexp = (Tok.FieldTok) dtok;
            fieldName = fexp.fieldName;
            funcTok = fexp.funcL.get(0);
        } else if (dtok instanceof Tok.FunctionTok) {
            Tok.FunctionTok fexp = (Tok.FunctionTok) dtok;
            funcTok = fexp;
        } else {
            DeliaExceptionHelper.throwNotImplementedError("z66");
        }

        DRule rule = null;
        RuleGuard guard = new AlwaysRuleGuard();
        String funcName = funcTok.fnName;
        switch (funcTok.fnName) {
            case "contains": {
                Tok.ValueTok vexp = (Tok.ValueTok) funcTok.argsL.get(0);
                DValue arg = vexp.value;
                RuleOperand oper = createOperand(fieldName, dtype, funcName);
                guard = adjustGuard(oper, guard);
                rule = new ContainsRule(guard, oper, arg.asString());
                break;
            }
            case "maxlen": {
                Tok.ValueTok vexp = (Tok.ValueTok) funcTok.argsL.get(0);
                DValue arg = vexp.value;
                RuleOperand oper = createOperand(fieldName, dtype, funcName);
                guard = adjustGuard(oper, guard);
                rule = new MaxLenRule(guard, oper, arg.asInt());
                break;
            }
            case "sizeof": {
                Tok.ValueTok vexp = (Tok.ValueTok) funcTok.argsL.get(0);
                DValue arg = vexp.value;
                RuleOperand oper = createOperand(fieldName, dtype, funcName);
                guard = adjustGuard(oper, guard);
                rule = new SizeofRule(guard, oper, arg.asInt());
                //adjust effective shape out in RuleBuilder
                break;
            }
            case "isDateOnly": {
                RuleOperand oper = createOperand(fieldName, dtype, funcName);
                guard = adjustGuard(oper, guard);
                rule = new IsDateOnlyRule(guard, oper);
                break;
            }
            case "isTimeOnly": {
                RuleOperand oper = createOperand(fieldName, dtype, funcName);
                guard = adjustGuard(oper, guard);
                rule = new IsTimeOnlyRule(guard, oper);
                break;
            }
            case "uniqueFields": {
                boolean haveSetGuard = false;
                List<RuleOperand> operL = new ArrayList<>();
                for (Tok.DToken exp : funcTok.argsL) {
                    Tok.FieldTok fieldExp = (Tok.FieldTok) exp;
                    RuleOperand oper = createOperand(fieldExp.fieldName, dtype, funcName);
                    operL.add(oper);
                    if (!haveSetGuard) {
                        haveSetGuard = true;
                        guard = adjustGuard(oper, guard);
                    }
                }
                rule = new UniqueFieldsRule(guard, operL);
                break;
            }
//		case "index":
//		{
//			boolean haveSetGuard = false;
//			List<RuleOperand> operL = new ArrayList<>();
//			for(Exp exp: qfe.argL) {
//				IdentExp arg = (IdentExp) exp;
//				RuleOperand oper = createOperand(arg.name(), dtype, qfe.funcName);
//				operL.add(oper);
//				if (!haveSetGuard) {
//					haveSetGuard = true;
//					guard = adjustGuard(oper, guard);
//				}
//			}
//			rule = new IndexRule(guard, operL);
//			break;
//		}
//		case "len":
//		{
//			rule = new LenFnRule(guard);
//			break;
//		}
//		case "year":
//		{
//			rule = new DateYearFnRule(guard);
//			break;
//		}
//		case "date":
//		{
//			//TODO: handle more args later
//			if (qfe.argL.isEmpty()) {
//				//err!!
//			} else {
//				rule = new DateMakeFnRule(factorySvc, guard, qfe.argL.get(0));
//			}
//
//			break;
//		}
            default:
                //error handled at higher level
                break;
        }

        if (rule != null && funcTok.negFlag) {
            rule.setPolarity(false);
        }
        return rule;
    }

    private RuleGuard adjustGuard(RuleOperand oper, RuleGuard guard) {
        List<String> fieldL = oper.getFieldList();
        if (CollectionUtils.isNotEmpty(fieldL)) {
            return new NotNullGuard(fieldL);
        }
        return guard;
    }

    private RuleOperand createOperand(String fieldName, DType dtype, String ruleName) {
        if (fieldName == null) {
            return new DValueRuleOperand();
        } else {
            if (!dtype.isStructShape()) {
                DeliaExceptionHelper.throwError("rule-not-allowed",
                        "Type %s: scalar types not allowed to use rule '%s' on field '%s'",
                        dtype.getName(), ruleName, fieldName);
            }
            DStructType structType = (DStructType) dtype;
            if (!structType.hasField(fieldName)) {
                DeliaExceptionHelper.throwError("rule-on-unknown-field",
                        "Type %s: rule '%s' on unknown field '%s'",
                        dtype.getName(), ruleName, fieldName);
            }
            return new StructDValueRuleOperand(fieldName);
        }
    }
}