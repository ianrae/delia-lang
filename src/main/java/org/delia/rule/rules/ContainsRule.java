package org.delia.rule.rules;

import org.delia.error.ErrorTracker;
import org.delia.rule.*;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.util.StringUtil;

public class ContainsRule extends DRuleBase {
		private RuleOperand oper1;
		private String arg1; //TODO: later a list
		
		public ContainsRule(RuleGuard guard, RuleOperand oper1, String arg1) {
			super("contains", guard);
			this.oper1 = oper1;
			this.arg1 = arg1;
		}

		@Override
		public String renderAsDelia(RuleGeneratorContext ctx) {
			return String.format("%scontains('%s')", ctx.getOperAsDelia(oper1), arg1);
		}


	@Override
		public void performCompilerPass4Checks(DType dtype, FieldExistenceService fieldExistSvc, ErrorTracker et) {
			fieldExistSvc.checkRuleOperand(getName(), oper1, et);
		}
		
		@Override
		protected boolean onValidate(DValue dval, DRuleContext ctx) {
			String s = oper1.asString(dval);
			
			if (! s.contains(arg1)) {
				String s1 = StringUtil.atMostChars(s, 80);
				String msg = String.format("%s needs to contain '%s': '%s'", getSubject(), arg1, s1);
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