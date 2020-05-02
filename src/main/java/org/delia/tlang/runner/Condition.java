package org.delia.tlang.runner;

import org.delia.type.DValue;

public interface Condition {
	void setTLangRunner(TLangRunner tlangRunner);
	boolean eval(DValue dval);
}