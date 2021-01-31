package org.delia.rule.rules;

import java.util.List;

import org.delia.rule.DRuleBase;
import org.delia.rule.DRuleContext;
import org.delia.rule.RuleGuard;
import org.delia.rule.RuleOperand;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

public class UniqueFieldsRule extends DRuleBase {
	private List<RuleOperand> operL;

	public UniqueFieldsRule(RuleGuard guard, List<RuleOperand> operL) {
		super("uniqueFields", guard);
		this.operL = operL;
	}
	@Override
	protected boolean onValidate(DValue dval, DRuleContext ctx) {
//		String fieldName = oper1.getSubject();
//		DValue inner = dval.asStruct().getField(fieldName);
//		
//		switch (inner.getType().getShape()) {
//		case INTEGER:
//			return validateInt(dval, ctx);
//		case LONG:
//			return checkLong(dval, ctx);
//		case STRING:
//			return validateString(dval, ctx);
//			default:
//				DeliaExceptionHelper.throwError("rule-wrong-field-type", "sizeof only supported on int and string types");
//			break;
//		}
		
		return true;
	}
	
	@Override
	public boolean dependsOn(String fieldName) {
		for(RuleOperand oper: operL) {
			if (oper.dependsOn(fieldName)) {
				return true;
			}
		}
		return false;
	}
	@Override
	public String getSubject() {
		if (operL.isEmpty()) return null;
		return operL.get(0).getSubject(); //TODO: is this ok
	}
}