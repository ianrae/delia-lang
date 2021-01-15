package org.delia.repl;

import java.io.IOException;

import org.delia.bdd.BDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.db.DBType;
import org.delia.h2.H2ConnectionHelper;
import org.delia.zdb.ZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;


public class ReplTests extends BDDBase {

	@Test
	public void test() throws IOException {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		DeliaRepl repl = new DeliaRepl(info, BASE_DIR);
		repl.run();
	}

//	@Test
	public void testH2() throws IOException {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.H2).connectionString(H2ConnectionHelper.getTestDB()).build();
		DeliaRepl repl = new DeliaRepl(info, BASE_DIR);
		repl.run();
	}

	//---
	private final String BASE_DIR = "src/main/resources/test/repl/";

	@Before
	public void init() {
	}
	@Override
	public ZDBInterfaceFactory createForTest() {
		return null;
	}

}
