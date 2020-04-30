package org.delia.runner.inputfunction;

import java.util.Iterator;
import java.util.List;

public class LineObjIterator implements Iterator<LineObj> {
	private List<LineObj> list;
	private int index;

	public LineObjIterator(List<LineObj> list) {
		this.list = list;
		this.index = 0;
	}

	@Override
	public boolean hasNext() {
		return index < list.size();
	}

	@Override
	public LineObj next() {
		LineObj con = list.get(index++);
		return con;
	}
}