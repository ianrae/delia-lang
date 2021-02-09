package org.delia.repl;

import java.io.IOException;

import org.delia.ConnectionStringBuilder;
import org.delia.bdd.BDDBase;
import org.delia.db.h2.H2ConnectionHelper;
import org.delia.db.sql.ConnectionString;
import org.delia.zdb.DBInterfaceFactory;
import org.junit.Before;


public class ReplTests extends BDDBase {

//	@Test
	public void test() throws IOException {
		ConnectionString connDef = ConnectionStringBuilder.createMEM();
		DeliaRepl repl = new DeliaRepl(connDef, BASE_DIR);
		repl.run();
	}

//	@Test
	public void testH2() throws IOException {
		ConnectionString connDef = H2ConnectionHelper.getTestDB();		
		DeliaRepl repl = new DeliaRepl(connDef, BASE_DIR);
		repl.run();
	}

	//---
	private final String BASE_DIR = "src/main/resources/test/repl/";

	@Before
	public void init() {
	}
	@Override
	public DBInterfaceFactory createForTest() {
		return null;
	}

}
