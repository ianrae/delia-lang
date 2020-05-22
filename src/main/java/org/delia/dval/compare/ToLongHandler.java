package org.delia.dval.compare;

import java.util.Date;

import org.delia.type.DValue;
import org.delia.type.Shape;

public class ToLongHandler implements Handler {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compareDVal(DValue dval1, DValue dval2) {
		Long n1;
		if (dval1.getType().isShape(Shape.DATE)) {
			n1 = dval1.asDate().getTime();
		} else {
			n1 = dval1.asLong();
		}
		Long n2;
		if (dval2.getType().isShape(Shape.DATE)) {
			n2 = dval2.asDate().getTime();
		} else {
			n2 = dval2.asLong();
		}
		return n1.compareTo(n2);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compare(Object obj1, Object obj2) {
		Long n1 = getLong(obj1);
		Long n2 = getLong(obj2);
		return n1.compareTo(n2);
	}
	private Long getLong(Object obj1) {
		if (obj1 instanceof DValue) {
			DValue dval = (DValue) obj1;
			if (dval.getType().isShape(Shape.DATE)) {
				return dval.asDate().getTime();
			} else {
				return dval.asLong();
			}
		} else if (obj1 instanceof Date) {
			Date dt = (Date) obj1;
			return dt.getTime();
		}
		
		Number n = (Number) obj1;
		return n.longValue();
	}
}