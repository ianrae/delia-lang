package org.delia.util;

import java.util.Iterator;
import java.util.List;

public class ListWalker<T> implements Iterator<T> {
	
	private List<T> list;
	private int index;

	public ListWalker(List<T> list) {
		this.list = list;
		this.index = 0;
	}

	@Override
	public boolean hasNext() {
		return index < list.size();
	}

	@Override
	public T next() {
		T con = list.get(index++);
		return con;
	}

	public boolean addIfNotLast(StrCreator sc, String... args) {
		if (! hasNext()) {
			return false;
		}
		for(String s: args) {
			sc.o(s);
		}
		return true;
	}
}
