package org.delia.bdd;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.bdd.core.BDDParser;
import org.delia.bdd.core.BDDTest;
import org.delia.bdd.core.BDDTestRunner;
import org.delia.bdd.core.BDDTester;
import org.delia.util.TextFileReader;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;

public class NewBDDTests extends BDDBase {

	@Test
	public void test() {
		tester.chkString("let a = 'abc'","abc");
	}

	@Test
	public void test2() {
		List<String> lines = build("", " ", "#comment", " #comment2");

		BDDParser parser = new BDDParser();
		List<BDDTest> tests = parser.parse(lines);
		assertEquals(0, tests.size());
	}
	@Test
	public void test3() {
		List<String> lines = build("FEATURE: a", "---", "title: a new test", "thenType: int", "let x int = 5");

		BDDParser parser = new BDDParser();
		List<BDDTest> tests = parser.parse(lines);
		assertEquals(1, tests.size());
		chkTest(tests, 0, "a new test");
		chkTestGiven(tests, 0, 0, 1, 0);
		chkTestWhen(tests, 0, "let x int = 5");
	}
	
	@Test
	public void test4() {
		List<String> lines = build("FEATURE: a", "---", "title: a new test", "thenType: int", "let x int = 5;5");

		BDDParser parser = new BDDParser();
		List<BDDTest> tests = parser.parse(lines);
		assertEquals(1, tests.size());
		chkTest(tests, 0, "a new test");
		chkTestGiven(tests, 0, 0, 1, 1);
		chkTestWhen(tests, 0, "let x int = 5");
		
		BDDTestRunner runner = new BDDTestRunner(this);
		ZDBInterfaceFactory dbInterface = new MemZDBInterfaceFactory(createFactorySvc());
		int passes = runner.runTests(tests, dbInterface);
		assertEquals(1, passes);
	}
	
	@Test
	public void test5() {
		List<String> lines = build("FEATURE: a", "---", "title: a new test", "thenType: int", "given:", "let z int = 7", "when:", "let x int = z;7");

		BDDParser parser = new BDDParser();
		List<BDDTest> tests = parser.parse(lines);
		assertEquals(1, tests.size());
		chkTest(tests, 0, "a new test");
		chkTestGiven(tests, 0, 1, 1, 1);
		chkTestWhen(tests, 0, "let x int = z");
		
		BDDTestRunner runner = new BDDTestRunner(this);
		ZDBInterfaceFactory dbInterface = new MemZDBInterfaceFactory(createFactorySvc());
		int passes = runner.runTests(tests, dbInterface);
		assertEquals(1, passes);
	}
	
	@Test
	public void test5fail() {
		List<String> lines = build("FEATURE: a", "---", "title: a new test", "thenType: int", "given:", "let z int = 7", "when:", "let x int = z;6");

		BDDParser parser = new BDDParser();
		List<BDDTest> tests = parser.parse(lines);
		assertEquals(1, tests.size());
		chkTest(tests, 0, "a new test");
		chkTestGiven(tests, 0, 1, 1, 1);
		chkTestWhen(tests, 0, "let x int = z");
		
		BDDTestRunner runner = new BDDTestRunner(this);
		ZDBInterfaceFactory dbInterface = new MemZDBInterfaceFactory(createFactorySvc());
		int passes = runner.runTests(tests, dbInterface);
		assertEquals(0, passes);
	}
	
	@Test
	public void test7() {
//		String path = "src/main/resources/test/bdd/R300-scalar/t0-int.txt";
		String path = testFile(BDDGroup.R300_scalar, "t0-int.txt");
		TextFileReader reader = new TextFileReader();
		List<String> lines = reader.readFile(path);
		BDDParser parser = new BDDParser();
		
		List<BDDTest> tests = parser.parse(lines);
		BDDTestRunner runner = new BDDTestRunner(this);
		ZDBInterfaceFactory dbInterface = new MemZDBInterfaceFactory(createFactorySvc());
		int passes = runner.runTests(tests, dbInterface);
		
		int numTests = 10;
		assertEquals(numTests, tests.size());
		assertEquals(numTests, passes);
		
		Long n = Long.MAX_VALUE;
		System.out.println(n);
		
	}

	//---
	private BDDTester tester; //not used. BDDTest2 is used

	@Before
	public void init() {
		tester = new BDDTester();
	}

	private void chkTest(List<BDDTest> tests, int i, String expected) {
		BDDTest test = tests.get(i);
		assertEquals(expected, test.title);

	}

	private List<String> build(String... args) {
		List<String> list = new ArrayList<>();
		for(String arg: args) {
			list.add(arg);
		}
		return list;
	}

	private void chkTestGiven(List<BDDTest> tests, int i, int iGiven, int iWhen, int iThen) {
		BDDTest test = tests.get(i);
		assertEquals(iGiven, test.givenL.size());
		assertEquals(iWhen, test.whenL.size());
		assertEquals(iThen, test.thenL.size());
	}
	private void chkTestWhen(List<BDDTest> tests, int index, String... args) {
		BDDTest test = tests.get(index);
		assertEquals(args.length, test.whenL.size());
		int i = 0;
		for(String arg: args) {
			assertEquals(arg, test.whenL.get(i++));
		}
	}

	@Override
	public ZDBInterfaceFactory createForTest() {
		return new MemZDBInterfaceFactory(createFactorySvc());
	}



}
