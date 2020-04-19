package org.delia.rule.rules;

import org.delia.rule.DRuleBase;
import org.delia.rule.DRuleContext;
import org.delia.rule.RuleGuard;
import org.delia.rule.RuleOperand;
import org.delia.type.DValue;
import org.delia.util.StringUtil;

public class MaxLenRule extends DRuleBase {
		private RuleOperand oper1;
		private int maxlen;
		
		public MaxLenRule(RuleGuard guard, RuleOperand oper1, int maxlen) {
			super("maxlen", guard);
			this.oper1 = oper1;
			this.maxlen = maxlen;
		}
		@Override
		protected boolean onValidate(DValue dval, DRuleContext ctx) {
			String s = oper1.asString(dval);
			
			if (s.length() > maxlen) {
				String s1 = StringUtil.atMostChars(s, 80);
				String msg = String.format("string longer than %d: '%s'", maxlen, s1);
				ctx.addError(this, msg);
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