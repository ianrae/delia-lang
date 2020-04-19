package org.delia.rule;

import org.delia.type.DValue;

public class AlwaysRuleGuard implements RuleGuard {

	@Override
	public boolean shouldExecRule(DValue dval) {
		return true;
	}

}
