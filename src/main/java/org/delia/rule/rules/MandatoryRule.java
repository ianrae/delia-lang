package org.delia.rule.rules;

import org.delia.rule.DRuleBase;
import org.delia.rule.DRuleContext;
import org.delia.rule.RuleGuard;
import org.delia.type.DStructType;
import org.delia.type.DValue;

public class MandatoryRule extends DRuleBase {
		private String fieldName;

		public MandatoryRule(RuleGuard guard, String fieldName) {
			super("mandatory", guard);
			this.fieldName = fieldName;
		}
		@Override
		protected boolean onValidate(DValue dval, DRuleContext ctx) {
			DStructType dtype = (DStructType) dval.getType();
			if (!dtype.fieldIsOptional(fieldName)) {
				DValue inner = dval.asStruct().getField(fieldName);
				
				if (inner == null) {
					String msg = String.format("Type '%s': mandatory field '%s' is null", dtype.getName(), fieldName);
					ctx.addError(this, msg);
					return false;
				}
			}
			return true;
		}
		@Override
		public boolean dependsOn(String fieldName) {
			return this.fieldName.equals(fieldName);
		}
		@Override
		public String getSubject() {
			return "mand?";
		}
	}