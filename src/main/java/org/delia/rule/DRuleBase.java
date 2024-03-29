package org.delia.rule;

import org.apache.commons.lang3.StringUtils;
import org.delia.type.DValue;

public abstract class DRuleBase implements DRule {
	private boolean polarity = true;
	private String name;
	private RuleGuard guard;
	
	public DRuleBase(String name, RuleGuard guard) {
		this.name = name;
		this.guard = guard;
	}
	public DRuleBase(String name, boolean polarity) {
		this.name = name;
		this.polarity = polarity;
	}
	
	@Override
	public boolean validate(DValue dval, DRuleContext ctx) {
		//we know dval.obj is not null
		//and we know its already been checked for valmode
		
		boolean pass = onValidate(dval, ctx);
		if (! polarity) {
			pass = !pass;
			if (! pass) {
				String subj = getSubject();
				subj = StringUtils.isEmpty(subj) ? "" : subj + "."; 
				String msg = String.format("NOT %s%s() failed", subj, name);
				ctx.addError(this, msg);
			}
		}

		return pass;
	}

	protected abstract boolean onValidate(DValue dval, DRuleContext ctx);
	
	public String getName() {
		return name;
	}
	
	@Override
	public Object exec(DValue dval, DRuleContext ctx) {
		return null;
	}
	@Override
	public void setPolarity(boolean polarity) {
		this.polarity = polarity;
	}
	@Override
	public boolean executeGuard(DValue dval) {
		boolean b = guard.shouldExecRule(dval);
		return b;
	}

//	protected String getOperAsDelia(RuleOperand oper1) {
//		return oper1.getSubject() == null ? "" : String.format("%s.", oper1.getSubject());
//	}

}