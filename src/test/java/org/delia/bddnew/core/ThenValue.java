package org.delia.bddnew.core;

import java.util.List;

public class ThenValue {
	public String expected;
	public List<String> expectedL = null;

	public ThenValue(String s) {
		this.expected = s;
	}
	public ThenValue(List<String> list) {
		this.expectedL = list;
	}
	
	public boolean isNull() {
		if (expectedL == null) {
			return expected.equals("null");
		} else {
			return false;
		}
	}

	public boolean isEmpty() {
		if (expectedL == null) {
			return expected == null;
		} else {
			return false;
		}
	}
}
