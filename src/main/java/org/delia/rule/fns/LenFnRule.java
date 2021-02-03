package org.delia.rule.fns;

import org.delia.error.ErrorTracker;
import org.delia.rule.DRuleBase;
import org.delia.rule.DRuleContext;
import org.delia.rule.FieldExistenceService;
import org.delia.rule.RuleGuard;
import org.delia.type.DValue;

public class LenFnRule extends DRuleBase {
		
		public LenFnRule(RuleGuard guard) {
			super("len", guard);
		}
		@Override
		public void performCompilerPass4Checks(FieldExistenceService fieldExistSvc, ErrorTracker et) {
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
			return false;
		}
		@Override
		public String getSubject() {
			return "";
		}
	}