package org.delia.runner.inputfunction;

import java.util.Iterator;

public interface LineObjIterator extends Iterator<LineObj> {

	int getNumHdrRows();
	String getFileName();
	void close();
}