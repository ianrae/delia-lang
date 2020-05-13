package org.delia.base;


public class DBTestHelper {

	//change this to true to disable all H2 and Postgres tests (they are slow)
	public static final boolean disableAllSlowTests = true;
	
	
	public static void throwIfNoSlowTests() {
		if (disableAllSlowTests) {
			throw new IllegalStateException("NO Slow tests!!!");
		}
	}
}
