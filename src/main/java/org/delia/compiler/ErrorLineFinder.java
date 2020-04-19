package org.delia.compiler;

public class ErrorLineFinder {
	private String src;
	
	public ErrorLineFinder(String src) {
		this.src = src;
	}
	public int findLineNum(int pos) {
		int lineNum = 1;
		for(int i = 0; i < src.length(); i++) {
			char ch = src.charAt(i);
			if (ch == '\n') {
				lineNum++;
			}
			
			if (i == pos) {
				return lineNum;
			}
		}

		return -1;
	}
}