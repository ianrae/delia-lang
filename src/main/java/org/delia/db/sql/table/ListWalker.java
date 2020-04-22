package org.delia.db.sql.table;

import java.util.Iterator;
import java.util.List;

import org.delia.db.sql.StrCreator;

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

	public void addIfNotLast(StrCreator sc, String... args) {
		if (! hasNext()) {
			return;
		}
		for(String s: args) {
			sc.o(s);
		}
	}
}
