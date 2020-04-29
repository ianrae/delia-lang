package org.delia.runner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.delia.type.DValue;

public class DValueIterator implements Iterator<DValue> {
	
	private List<DValue> list;
	private int index;

	public DValueIterator(List<DValue> list) {
		this.list = list;
		this.index = 0;
	}
	public DValueIterator(DValue dval) {
		this.list = new ArrayList<>();
		this.list.add(dval);
		this.index = 0;
	}
	
	@Override
	public boolean hasNext() {
		return index < list.size();
	}

	@Override
	public DValue next() {
		DValue con = list.get(index++);
		return con;
	}
}
