package org.delia.tlang.runner;

import org.delia.type.DValue;

public class IsMissingCondition implements Condition {

	public IsMissingCondition() {
	}
	@Override
	public boolean eval(DValue dval) {
		return dval == null || dval.asString().isEmpty();
	}
	@Override
	public void setTLangRunner(TLangRunner tlangRunner) {
	}
}