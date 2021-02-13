package org.delia.rule.rules;

import java.util.List;

import org.delia.error.ErrorTracker;
import org.delia.rule.DRuleBase;
import org.delia.rule.DRuleContext;
import org.delia.rule.FieldExistenceService;
import org.delia.rule.RuleGuard;
import org.delia.rule.RuleOperand;
import org.delia.type.DValue;

public class IndexRule extends DRuleBase {
	private List<RuleOperand> operL;

	public IndexRule(RuleGuard guard, List<RuleOperand> operL) {
		super("index", guard);
		this.operL = operL;
	}
	
	@Override
	public void performCompilerPass4Checks(FieldExistenceService fieldExistSvc, ErrorTracker et) {
		for(RuleOperand oper: operL) {
			fieldExistSvc.checkRuleOperand(getName(), oper, et);
		}
	}
	
	@Override
	protected boolean onValidate(DValue dval, DRuleContext ctx) {
		if (ctx.getDBCapabilities().supportsUniqueConstraint()) {
			return true; //db will do this validation
		}
		
		if (operL.isEmpty()) {
			return true;
		}
		
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
	public List<RuleOperand> getOperList() {
		return operL;
	}
}