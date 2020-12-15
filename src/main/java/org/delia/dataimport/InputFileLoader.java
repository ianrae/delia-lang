package org.delia.dataimport;

import org.delia.runner.inputfunction.LineObjIterator;


public interface InputFileLoader extends LineObjIterator {

	void init(String path);
	void open(String path);
	void close();
	char getDelim();

}