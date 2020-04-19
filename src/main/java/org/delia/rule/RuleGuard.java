package org.delia.rule;

import org.delia.type.DValue;

public interface RuleGuard {
	boolean shouldExecRule(DValue dval);
}
