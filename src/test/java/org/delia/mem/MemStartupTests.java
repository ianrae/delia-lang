package org.delia.mem;

import org.delia.db.DBType;
import org.delia.h2.DeliaInitializer;
import org.delia.h2.H2StartupTests;
import org.delia.runner.Runner;
import org.junit.Test;

public class MemStartupTests extends H2StartupTests {

	@Test
	public void testDB() throws Exception {
		DeliaInitializer initter = new DeliaInitializer();
		Runner runner = initter.init(DBType.MEM);
		basicTest(initter, runner);
	}
	
}
