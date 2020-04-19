package org.delia.rule.fns;

import org.delia.rule.DRuleBase;
import org.delia.rule.DRuleContext;
import org.delia.rule.RuleGuard;
import org.delia.type.DValue;

public class LenFnRule extends DRuleBase {
		
		public LenFnRule(RuleGuard guard) {
			super("len", guard);
		}
		@Override
		protected boolean onValidate(DValue dval, DRuleContext ctx) {
			return false; //not allowed!
		}
		@Override
		public Object exec(DValue dval, DRuleContext ctx) {
			String s = dval.asString();
			return s.length();
		}
		@Override
		public boolean dependsOn(String fieldName) {
			// TODO fix this. what are we getting the length of?
			return false;
		}
		@Override
		public String getSubject() {
			return "";
		}
	}