package org.delia.runner.inputfunction;

import java.util.List;

public class LineObjIteratorImpl implements LineObjIterator {
	private List<LineObj> list;
	private int index;

	public LineObjIteratorImpl(List<LineObj> list) {
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

	@Override
	public int getNumHdrRows() {
		return 0;
	}

	@Override
	public String getFileName() {
		return "in-memory";
	}

	@Override
	public void close() {
	}

	@Override
	public LineObj readHdrRow() {
		return null;
	}
}