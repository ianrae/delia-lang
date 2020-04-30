package org.delia.tlang.runner;

import org.delia.type.DValue;

public class BasicCondition implements Condition {
	public boolean flag;

	public BasicCondition(boolean b) {
		this.flag = b;
	}
	@Override
	public boolean eval(DValue dval) {
		return flag;
	}
	@Override
	public void setTLangRunner(TLangRunner tlangRunner) {
	}
}