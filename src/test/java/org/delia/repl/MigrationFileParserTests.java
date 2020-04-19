package org.delia.repl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.schema.SchemaType;
import org.delia.repl.migration.MigrationParser;
import org.delia.repl.migration.MigrationStep;
import org.delia.scope.scopetest.relation.DeliaClientTestBase;
import org.junit.Before;
import org.junit.Test;


public class MigrationFileParserTests extends DeliaClientTestBase {
	
	@Test
	public void test() {
		MigrationParser parser = createParser();
		List<String> lines = new ArrayList<>();
		chkParser(parser, lines, 0);
		
		lines.add("");
		lines.add(" ");
		lines.add(" # a comment ");
		chkParser(parser, lines, 0);
	}
	
	@Test
	public void testFail() {
		MigrationParser parser = createParser();
		List<String> lines = new ArrayList<>();
		chkParser(parser, lines, 0);
		
		lines.add("asldfj lsjefs");
		chkParserFail(parser, lines, 0, 1);
	}
	
	@Test
	public void testFail2() {
		MigrationParser parser = createParser();
		List<String> lines = new ArrayList<>();
		chkParser(parser, lines, 0);
		
		lines.add("create-table");
		lines.add("delete-table");
		lines.add("rename-table");
		chkParserFail(parser, lines, 3, 3);
	}
	
	@Test
	public void testFail3() {
		MigrationParser parser = createParser();
		List<String> lines = new ArrayList<>();
		chkParser(parser, lines, 0);
		
		lines.add("add-field");
		lines.add("delete-field");
		lines.add("rename-field");
		lines.add("alter-field");
		chkParserFail(parser, lines, 4, 4);
	}

	
	@Test
	public void testOK() {
		MigrationParser parser = createParser();
		List<String> lines = new ArrayList<>();
		chkParser(parser, lines, 0);
		
		lines.add("create-table C");
		lines.add("delete-table C");
		lines.add("rename-table C C2");
		lines.add("add-field C.f");
		lines.add("delete-field C.f");
		lines.add("rename-field C.f f2");
		lines.add("alter-field C.f");
		chkParser(parser, lines, 7);
		
		MigrationStep step = steps.get(5);
		assertEquals("rename-field", step.name);
		assertEquals("C.f", step.arg1);
		assertEquals("f2", step.arg2);
		
		List<SchemaType> sts = parser.convertToSchemaType(steps);
		assertEquals(7, sts.size());
		SchemaType st = sts.get(5);
		assertEquals("U", st.action);
		assertEquals("C", st.typeName);
		assertEquals("f", st.field);
		assertEquals("f2", st.newName);
	}

	//---
	private List<MigrationStep> steps = new ArrayList<>();

	@Before
	public void init() {
		super.init();
	}
	
	private void chkParser(MigrationParser parser, List<String> lines, int nExpected) {
		boolean b = parser.parse(lines, steps);
		assertEquals(true, b);
		assertEquals(nExpected, steps.size());
		assertEquals(0, parser.getFailCount());
	}
	private void chkParserFail(MigrationParser parser, List<String> lines, int nExpected, int numFail) {
		boolean b = parser.parse(lines, steps);
		assertEquals(false, b);
		assertEquals(nExpected, steps.size());
		assertEquals(numFail, parser.getFailCount());
	}


	private MigrationParser createParser() {
		return new MigrationParser(factorySvc);
	}


}
