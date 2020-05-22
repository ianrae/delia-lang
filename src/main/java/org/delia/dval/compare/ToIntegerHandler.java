package org.delia.dval.compare;

import org.delia.type.DValue;

public class ToIntegerHandler implements Handler {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compareDVal(DValue dval1, DValue dval2) {
		Integer n1 = dval1.asInt();
		Integer n2 = dval2.asInt();
		return n1.compareTo(n2);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compare(Object obj1, Object obj2) {
		Integer n1 = getInteger(obj1);
		Integer n2 = getInteger(obj2);
		return n1.compareTo(n2);
	}
	private Integer getInteger(Object obj1) {
		if (obj1 instanceof DValue) {
			DValue dval = (DValue) obj1;
			return dval.asInt();
		}
		Number n = (Number) obj1;
		return n.intValue();
	}
}