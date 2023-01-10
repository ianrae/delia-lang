package org.delia.dval.compare;

import org.delia.type.DValue;

public class ToNumberHandler implements Handler {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compareDVal(DValue dval1, DValue dval2) {
		Double n1 = dval1.asNumber();
		Double n2 = dval2.asNumber();
		return n1.compareTo(n2);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compare(Object obj1, Object obj2) {
		Double n1 = getDouble(obj1);
		Double n2 = getDouble(obj2);
		return n1.compareTo(n2);
	}
	private Double getDouble(Object obj1) {
		if (obj1 instanceof DValue) {
			DValue dval = (DValue) obj1;
			return dval.asNumber();
		}
		Number n = (Number) obj1;
		return n.doubleValue();
	}
}