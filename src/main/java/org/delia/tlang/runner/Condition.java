package org.delia.tlang.runner;

import org.delia.type.DValue;

public interface Condition {
	boolean eval(DValue dval);
}