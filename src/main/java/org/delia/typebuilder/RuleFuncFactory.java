package org.delia.typebuilder;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.core.FactoryService;
import org.delia.rule.AlwaysRuleGuard;
import org.delia.rule.DRule;
import org.delia.rule.DValueRuleOperand;
import org.delia.rule.NotNullGuard;
import org.delia.rule.RuleGuard;
import org.delia.rule.RuleOperand;
import org.delia.rule.StructDValueRuleOperand;
import org.delia.rule.fns.DateMakeFnRule;
import org.delia.rule.fns.DateYearFnRule;
import org.delia.rule.fns.LenFnRule;
import org.delia.rule.rules.ContainsRule;
import org.delia.rule.rules.MaxLenRule;
import org.delia.rule.rules.SizeofRule;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.util.DeliaExceptionHelper;

public class RuleFuncFactory {
	private FactoryService factorySvc;
	
	public RuleFuncFactory(FactoryService factorySvc) {
		this.factorySvc = factorySvc;
	}

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