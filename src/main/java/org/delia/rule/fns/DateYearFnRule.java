package org.delia.rule.fns;

import java.util.Date;

import org.delia.rule.DRuleBase;
import org.delia.rule.DRuleContext;
import org.delia.rule.RuleGuard;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.StringUtil;

public class DateYearFnRule extends DRuleBase {
		
		public DateYearFnRule(RuleGuard guard) {
			super("year", guard);
		}
		@Override
		protected boolean onValidate(DValue dval, DRuleContext ctx) {
			return false; //not allowed!
		}
		@Override
		public Object exec(DValue dval, DRuleContext ctx) {
			if (! dval.getType().isShape(Shape.DATE)) {
				String s1 = StringUtil.atMostChars(dval.asString(), 80);
				String msg = String.format("%s can only be used on date values: '%s'", getName(), s1);
				ctx.addError(this, msg);
				return false;
			}
			
			Date dt = dval.asDate();
			int yr = dt.getYear() + 1900; //TODO fix
			return yr;
		}
		@Override
		public boolean dependsOn(String fieldName) {
			return false;
		}
		@Override
		public String getSubject() {
			return "";
		}
	}