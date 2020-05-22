package org.delia.dval.compare;

import org.delia.type.DValue;

public class ComparableDValueHandler implements Handler {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compareDVal(DValue dval1, DValue dval2) {
		Comparable c1 = (Comparable) dval1.getObject();
		Comparable c2 = (Comparable) dval2.getObject();
		return c1.compareTo(c2);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compare(Object obj1, Object obj2) {
		Comparable c1 = getComparable(obj1); 
		Comparable c2 = getComparable(obj2); 
		return c1.compareTo(c2);
	}
	private Comparable getComparable(Object obj1) {
		if (obj1 instanceof DValue) {
			DValue dval = (DValue) obj1;
			return (Comparable) dval.getObject();
		}
		return (Comparable) obj1;
	}
}