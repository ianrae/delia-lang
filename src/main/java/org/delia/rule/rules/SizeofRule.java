package org.delia.rule.rules;

import org.delia.rule.DRuleBase;
import org.delia.rule.DRuleContext;
import org.delia.rule.RuleGuard;
import org.delia.rule.RuleOperand;
import org.delia.type.DValue;

public class SizeofRule extends DRuleBase {
	private RuleOperand oper1;
	private int maxlen;

	public SizeofRule(RuleGuard guard, RuleOperand oper1, int maxlen) {
		super("sizeof", guard);
		this.oper1 = oper1;
		this.maxlen = maxlen;
	}
	@Override
	protected boolean onValidate(DValue dval, DRuleContext ctx) {
		int n = oper1.asInt(dval);

		if (n > 255) {
			String msg = String.format("int sss longer than %d: n", maxlen, n);
			ctx.addError(this, msg, oper1);
			return false;
		}
		return true;
	}
	@Override
	public boolean dependsOn(String fieldName) {
		return oper1.dependsOn(fieldName);
	}
	@Override
	public String getSubject() {
		return oper1.getSubject();
	}
}