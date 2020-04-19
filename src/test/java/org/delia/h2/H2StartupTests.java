package org.delia.h2;

import org.delia.db.DBType;
import org.delia.runner.LegacyRunner;
import org.junit.Test;


public class H2StartupTests extends StartupTestBase {

	@Test
	public void testDB() throws Exception {
		DeliaInitializer initter = new DeliaInitializer();
		LegacyRunner runner = initter.init(DBType.H2);
		basicTest(initter, runner);
	}
}
