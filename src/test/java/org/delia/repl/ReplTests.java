package org.delia.repl;

import java.io.IOException;

import org.delia.ConnectionDefinitionBuilder;
import org.delia.bdd.BDDBase;
import org.delia.db.h2.H2ConnectionHelper;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.zdb.DBInterfaceFactory;
import org.junit.Before;


public class ReplTests extends BDDBase {

//	@Test
	public void test() throws IOException {
		ConnectionDefinition connDef = ConnectionDefinitionBuilder.createMEM();
		DeliaRepl repl = new DeliaRepl(connDef, BASE_DIR);
		repl.run();
	}

//	@Test
	public void testH2() throws IOException {
		ConnectionDefinition connDef = H2ConnectionHelper.getTestDB();		
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
