package org.delia.dval.compare;

import org.delia.type.DValue;

public class ToBooleanHandler implements Handler {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compareDVal(DValue dval1, DValue dval2) {
		Boolean n1 = dval1.asBoolean();
		Boolean n2 = dval2.asBoolean();
		return n1.compareTo(n2);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compare(Object obj1, Object obj2) {
		Boolean n1 = getBoolean(obj1);
		Boolean n2 = getBoolean(obj2);
		return n1.compareTo(n2);
	}
	private Boolean getBoolean(Object obj1) {
		if (obj1 instanceof DValue) {
			DValue dval = (DValue) obj1;
			return dval.asBoolean();
		}
		
		Boolean b = (Boolean) obj1;
		return b;
	}
}