package org.delia.parser;

import org.delia.compiler.ast.TypeStatementExp;
import org.delia.runner.CompilerHelper;
import org.delia.runner.RunnerHelper;
import org.junit.Before;
import org.junit.Test;



public class TypeParserTests {
	
	@Test
	public void testEmpty() {
		chkType("type Actor struct {} end", "type Actor struct { } end");
	}
	@Test
	public void test1() {
		chkType("type Actor struct {name string} end", "type Actor struct {name string } end");
	}
	@Test
	public void test2() {
		chkType("type Actor struct {name string, points int, flag boolean} end", "type Actor struct {name string, points int, flag boolean } end");
	}
	@Test
	public void testOptional() {
		chkType("type Actor struct {name string optional} end", "type Actor struct {name string optional } end");
	}
	@Test
	public void testUnique() {
		chkType("type Actor struct {name string unique} end", "type Actor struct {name string unique } end");
		chkType("type Actor struct {name string optional unique} end", "type Actor struct {name string optional unique } end");
		chkType("type Actor struct {name string unique optional} end", "type Actor struct {name string optional unique } end");
	}
	@Test
	public void testPrimaryKey() {
		chkType("type Actor struct {name string primaryKey} end", "type Actor struct {name string primaryKey } end");
	}
	@Test
	public void testRelationOne() {
		String src = "type Address struct {x int, y int} end\n";
		chelper.parseTwo(src + "type Actor struct {relation addr Address one} end");
		chelper.parseTwo(src + "type Actor struct {relation addr Address one unique} end");
		chelper.parseTwo(src + "type Actor struct {relation addr Address one optional} end");
	}
	@Test
	public void testRelationMany() {
		String src = "type Address struct {x int, y int} end\n";
		chelper.parseTwo(src + "type Actor struct {relation addr Address many} end");
		chelper.parseTwo(src + "type Actor struct {relation addr Address many unique} end");
		chelper.parseTwo(src + "type Actor struct {relation addr Address many optional} end");
	}
	
	@Test
	public void testRule() {
		TypeStatementExp typeExp = chkType("type Point struct {x int, y int} x < 10 end", "type Point struct {x int, y int } x < 10 end");
		RuleHelper.chkRules(typeExp, 1);
		RuleHelper.chkOpRule(typeExp.ruleSetExp.ruleL.get(0), "x", "<", "10");
	}
	
	// --
	private RunnerHelper helper = new RunnerHelper();
	private CompilerHelper chelper = new CompilerHelper(null);
	
	//---
	@Before
	public void init() {
	}
	
	
	private TypeStatementExp chkType(String input, String output) {
		return chelper.chkType(input, output);
	}

}
