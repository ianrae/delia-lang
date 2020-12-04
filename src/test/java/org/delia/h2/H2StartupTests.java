package org.delia.h2;

import org.delia.base.DBTestHelper;
import org.delia.db.DBType;
import org.delia.runner.LegacyRunner;
import org.junit.Test;


public class H2StartupTests extends StartupTestBase {

	@Test
	public void testDB() throws Exception {
		DBTestHelper.throwIfNoSlowTests();

		DeliaInitializer initter = new DeliaInitializer();
		LegacyRunner runner = initter.init(DBType.H2);
		basicTest(initter, runner.innerRunner);
	}
}
