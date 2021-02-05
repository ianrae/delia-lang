package org.delia.rule;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.core.FactoryService;
import org.delia.rule.fns.DateMakeFnRule;
import org.delia.rule.fns.DateYearFnRule;
import org.delia.rule.fns.LenFnRule;
import org.delia.rule.rules.ContainsRule;
import org.delia.rule.rules.MaxLenRule;
import org.delia.rule.rules.SizeofRule;
import org.delia.rule.rules.UniqueFieldsRule;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.util.DeliaExceptionHelper;

public class DefaultRuleFunctionBuilder implements RuleFunctionBulder {
	private FactoryService factorySvc;
	
	public DefaultRuleFunctionBuilder(FactoryService factorySvc) {
		this.factorySvc = factorySvc;
	}

	/* (non-Javadoc)
	 * @see org.delia.typebuilder.RuleFunctionFactory#createRule(org.delia.compiler.astx.XNAFMultiExp, int, org.delia.type.DType)
	 */
	@Override
	public DRule createRule(XNAFMultiExp rfe, int index, DType dtype) {
		//only one func. TODO: fix later!!
		XNAFSingleExp qfe0 = rfe.qfeL.get(index);
		XNAFSingleExp qfe = qfe0;
		
		String fieldName = null;
		if (rfe.qfeL.size() == 2) {
			XNAFSingleExp qfe1 = rfe.qfeL.get(1);
			if (!qfe0.isRuleFn && qfe1.isRuleFn) {
				fieldName = qfe0.funcName;
				qfe = qfe1;
			}
		}
		
		
		DRule rule = null;
		RuleGuard guard = new AlwaysRuleGuard();
		switch(qfe.funcName) {
		case "contains":
		{
			StringExp arg = (StringExp) qfe.argL.get(0);
			RuleOperand oper = createOperand(fieldName, dtype, qfe.funcName);
			guard = adjustGuard(oper, guard);
			rule = new ContainsRule(guard, oper, arg.strValue());
			break;
		}
		case "maxlen":
		{
			IntegerExp arg = (IntegerExp) qfe.argL.get(0);
			RuleOperand oper = createOperand(fieldName, dtype, qfe.funcName);
			guard = adjustGuard(oper, guard);
			rule = new MaxLenRule(guard, oper, arg.val);
			break;
		}
		case "sizeof":
		{
			IntegerExp arg = (IntegerExp) qfe.argL.get(0);
			RuleOperand oper = createOperand(fieldName, dtype, qfe.funcName);
			guard = adjustGuard(oper, guard);
			rule = new SizeofRule(guard, oper, arg.val);
			break;
		}
		case "uniqueFields":
		{
			boolean haveSetGuard = false;
			List<RuleOperand> operL = new ArrayList<>();
			for(Exp exp: qfe.argL) {
				IdentExp arg = (IdentExp) exp;
				RuleOperand oper = createOperand(arg.name(), dtype, qfe.funcName);
				operL.add(oper);
				if (!haveSetGuard) {
					haveSetGuard = true;
					guard = adjustGuard(oper, guard);
				}
			}
			rule = new UniqueFieldsRule(guard, operL);
			break;
		}
		case "index":
		{
			boolean haveSetGuard = false;
			List<RuleOperand> operL = new ArrayList<>();
			for(Exp exp: qfe.argL) {
				IdentExp arg = (IdentExp) exp;
				RuleOperand oper = createOperand(arg.name(), dtype, qfe.funcName);
				operL.add(oper);
				if (!haveSetGuard) {
					haveSetGuard = true;
					guard = adjustGuard(oper, guard);
				}
			}
			rule = new UniqueFieldsRule(guard, operL);
			break;
		}
		case "len":
		{
			rule = new LenFnRule(guard);
			break;
		}
		case "year":
		{
			rule = new DateYearFnRule(guard);
			break;
		}
		case "date":
		{
			//TODO: handle more args later
			if (qfe.argL.isEmpty()) {
				//err!!
			} else {
				rule = new DateMakeFnRule(factorySvc, guard, qfe.argL.get(0));
			}
			
			break;
		}
		default:
			//error handled at higher level
			break;
		}
		
		if (rule != null) {
			rule.setPolarity(rfe.polarity);
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
			if (! dtype.isStructShape()) {
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