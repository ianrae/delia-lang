package org.delia.parser;

import static org.junit.Assert.assertEquals;

public class LineChecker {
	private String str;
	private int index;
	
	public LineChecker(String str) {
		this.str = str;
		this.index = 0;
	}
	
	public void chkLine(String expected) {
		chkLine(str, index++, expected);
	}
	public void chkLine(String sql, int i, String expected) {
		String[] ar = sql.split("\n");
		String line = ar[i];
		assertEquals(expected.trim(), line.trim());
	}
}