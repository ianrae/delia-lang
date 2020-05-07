package org.delia.base;


public class DBTestHelper {

	public static final boolean disableAllSlowTests = true;
	
	
	public static void throwIfNoSlowTests() {
		if (disableAllSlowTests) {
			throw new IllegalStateException("NO Slow tests!!!");
		}
	}
}
