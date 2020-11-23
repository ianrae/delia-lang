package org.delia.repl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.delia.base.UnitTestLog;
import org.delia.bdd.BDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.db.DBType;
import org.delia.log.Log;
import org.delia.runner.ResultValue;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;


public class ReplRunnerTests extends BDDBase {
	
	@Test
	public void testFile() {
		String path = BASE_DIR + "delia1.txt";
		ResultValue res = runFile(path);
		chkOK(res, "created variable x (int) = 5");
	}
	
	@Test
	public void testTypes() {
		ResultValue res = run("let x int = 5");
		chkOK(res, "created variable x (int) = 5");
		
		res = run("let x2 long = 5");
		chkOK(res, "created variable x2 (long) = 5");

		res = run("let x3 number = -34.5");
		chkOK(res, "created variable x3 (number) = -34.5");

		res = run("let x4 boolean = true");
		chkOK(res, "created variable x4 (boolean) = true");

		res = run("let x5 string = 'abc'");
		chkOK(res, "created variable x5 (string) = 'abc'");
		
		res = run("let x6 date = '2020'");
		chkOK(res, "created variable x6 (date) = 2020-01-01T00:00:00.000+0000");
	}
	
	@Test
	public void testErr() {
		ResultValue res = run("let x int = z");
		chkFail(res, "int value is not an int - z");
	}	
	
	@Test
	public void testStructOne() {
		String path = BASE_DIR + "delia2.txt";
		ResultValue res = runFile(path);
		chkOKLine(res, "created variable x (Flight) = {");
		chkLine(res, "field1:55");
		chkLine(res, "}");
	}
	
	@Test
	public void testStructMulti() {
		String path = BASE_DIR + "delia3.txt";
		ResultValue res = runFile(path);
		chkOKLine(res, "created variable x (Flight) = [{");
		chkLine(res, "field1:55");
		chkLine(res, "}{");
		chkLine(res, "field1:56");
		chkLine(res, "}]");
	}
	
	@Test
	public void test2() {
		ResultValue res = run("let x int = 5");
		chkOK(res, "created variable x (int) = 5");
		
		res = runContinue("let y long = 15");
		chkOK(res, "created variable y (long) = 15");
		
		res = runContinue("? x");
		chkOK(res, "5");
		
		res = runContinue("? y");
		chkOK(res, "15");
		
		res = runContinue("? z");
		chkFail(res, "can't find variable: z");
	}	
	
	@Test
	public void testStartEmptyAndRun() {
		ResultValue res = run("");
		assertEquals(null, res.val);
		
		String path = BASE_DIR + "delia2.txt";
		
		res = run(String.format("run %s", path));
		chkOKLine(res, "created variable x (Flight) = {");
		chkLine(res, "field1:55");
		chkLine(res, "}");
	}
	
	@Test
	public void testMigrationPlan() {
		ResultValue res = run("");
		assertEquals(null, res.val);
		
		String path = BASE_DIR + "delia2.txt";
		res = run(String.format("load %s", path));
		assertEquals(true, res.ok);
		assertEquals(null, res.val);
		
		res = run("mg");
		assertEquals(true, res.ok);
		assertNotNull(null, res.val);
		
		res = run("mr");
		assertEquals(true, res.ok);
		assertNotNull(null, res.val);
	}
	
	@Test
	public void testMigrationRejectedPolicy() {
		String path = BASE_DIR + "delia4.txt";
		ResultValue res = run(String.format("run %s", path));
		chkOKLine(res, "");
		
		log.log("we change Flight type...");
//		runner.restart();
		path = BASE_DIR + "delia2.txt";
		res = run(String.format("run %s", path));
//		res = run("run C:/DAILY/apr8/delia2.txt");
		assertEquals(true, res.ok);
		assertEquals("", mostRecentREPLResultStr);
	}
	
	//---
	private final String BASE_DIR = "src/main/resources/test/repl/";
	ReplRunner runner;
	private Log log = new UnitTestLog();

	@Before
	public void init() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		runner = new ReplRunner(info, new ConsoleOutputWriter());
	}
	
	int currentLineNum = 0;
	String mostRecentREPLResultStr = null;
	private void chkOKLine(ResultValue res, String expected) {
		assertEquals(true, res.ok);
		chkLine(res, expected);
	}
	private void chkLine(ResultValue res, String expected) {
		String s = runner.toReplResult(res);
		String[] ar = s.split("\n");
		String line = ar[currentLineNum++].trim();
		
		expected = expected.trim();
		assertEquals(expected, line);
	}

	private ResultValue runFile(String path) {
		ResultValue res = runner.runFromFile(path);
		String s = runner.toReplResult(res);
		log.log(s);
		return res;
	}

	private ResultValue run(String src) {
		ResultValue res = runner.executeReplCmdOrDelia(src);
		if (res != null) {
			String s = runner.toReplResult(res);
			log.log(s);
			mostRecentREPLResultStr = s;
		}
		return res;
	}
	private ResultValue runContinue(String src) {
		ResultValue res = runner.executeReplCmdOrDelia(src);
		String s = runner.toReplResult(res);
		log.log(s);
		return res;
	}

	
	private void chkOK(ResultValue res, String expected) {
		assertEquals(true, res.ok);
		// OK: 5  (int)
		String s = runner.toReplResult(res);
		assertEquals(expected, s);
	}
	private void chkFail(ResultValue res, String expected) {
		assertEquals(false, res.ok);
		String s = runner.toReplResult(res);
//		assertEquals(expected, s);
		assertEquals(true, s.contains(expected));
	}

	@Override
	public ZDBInterfaceFactory createForTest() {
		MemZDBInterfaceFactory db = new MemZDBInterfaceFactory(createFactorySvc());
		return db;
	}


}
