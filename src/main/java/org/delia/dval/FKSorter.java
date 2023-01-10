package org.delia.dval;

import org.delia.type.DValue;

import java.util.Comparator;


public class FKSorter implements Comparator<DValue> {
	private boolean isAsc;
	
	public FKSorter(boolean isAsc) {
		this.isAsc = isAsc;
	}
	@Override
	public int compare(DValue arg0, DValue arg1) {
		if (arg0 == null || arg1 == null) {
			return 0;
		}
		
		@SuppressWarnings("rawtypes")
		Comparable c0 = (Comparable) arg0.getObject();
		@SuppressWarnings("rawtypes")
		Comparable c1 = (Comparable) arg1.getObject();
		if (isAsc) {
			return c0.compareTo(c1);
		} else {
			return c1.compareTo(c0);
		}
	}
}
