package org.delia.dval.compare;

import org.delia.type.DValue;

public class ToStringHandler implements Handler {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compareDVal(DValue dval1, DValue dval2) {
		String n1 = dval1.asString();
		String n2 = dval2.asString();
		return n1.compareTo(n2);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compare(Object obj1, Object obj2) {
		String n1 = obj1.toString();
		String n2 = obj2.toString();
		return n1.compareTo(n2);
	}
}