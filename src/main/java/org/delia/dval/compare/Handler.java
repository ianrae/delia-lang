package org.delia.dval.compare;

import org.delia.type.DValue;

public interface Handler {
	int compareDVal(DValue dval1, DValue dval2);
	int compare(Object obj1, Object obj2);
}