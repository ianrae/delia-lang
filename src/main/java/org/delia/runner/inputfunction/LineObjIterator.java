package org.delia.runner.inputfunction;

import java.util.Iterator;

public interface LineObjIterator extends Iterator<LineObj> {
	LineObj readHdrRow(); //MUST be called first. may return NULL if none
	int getNumHdrRows(); //0 or 1 now. later we'll support multiple
	String getFileName();
	void close();
}