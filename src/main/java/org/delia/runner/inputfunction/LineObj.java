package org.delia.runner.inputfunction;

import java.util.Arrays;
import java.util.List;

public class LineObj {
	public String[] elements;
	public int lineNum;
	
	public LineObj(String[] ar, int lineNum) {
		this.elements = ar;
		this.lineNum = lineNum;
	}
	
	public List<String> toList() {
		return Arrays.asList(elements);
	}
}