package org.delia.parser;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.compiler.DeliaCompiler;
import org.delia.compiler.ast.Exp;
import org.delia.runner.DeliaException;
import org.junit.Test;

public class CompilerPass2Tests extends ParserTestBase {
	
	@Test(expected=DeliaException.class)
	public void testSyntaxError() {
		DeliaCompiler compiler = initCompiler();
		compiler.parse("letxxx a = 10"); //throws
	}
	@Test
	public void testOK() {
		DeliaCompiler compiler = initCompiler();
		List<Exp> list = compiler.parse("let a = 10");
		assertEquals(1, list.size());
	}
//	@Test(expected=DangException.class)
//	public void testPass2Error() {
//		DangCompiler compiler = initCompiler();
//		List<Exp> list = compiler.parse("let a = Actor[id < null]");
//		assertEquals(1, list.size());
//	}
//	@Test(expected=DangException.class)
//	public void testPass2ErrorLine2() {
//		DangCompiler compiler = initCompiler();
//		List<Exp> list = compiler.parse("let a = 5\nlet b = Actor[id < null]");
//		assertEquals(1, list.size());
//	}
	
	@Test(expected=DeliaException.class)
	public void testRelationError() {
		DeliaCompiler compiler = initCompiler();
		String src = "type Point struct {relation x int, y int} end";
		compiler.parse(src);
	}
	@Test(expected=DeliaException.class)
	public void testOneError() {
		DeliaCompiler compiler = initCompiler();
		String src = "type Point struct {x int one, y int} end";
		compiler.parse(src);
	}
	@Test(expected=DeliaException.class)
	public void testManyError() {
		DeliaCompiler compiler = initCompiler();
		String src = "type Point struct {x int many, y int} end";
		compiler.parse(src);
	}

	@Test(expected=DeliaException.class)
	public void testRelationMissingError() {
		DeliaCompiler compiler = initCompiler();
		String src = "type Point struct {x int many, y int} end\n";
		src += "type Shape struct {pt Point, y int} end";
		compiler.parse(src);
	}
	@Test(expected=DeliaException.class)
	public void testOneOrManyMissingError() {
		DeliaCompiler compiler = initCompiler();
		String src = "type Point struct {x int many, y int} end\n";
		src += "type Shape struct {relation pt Point, y int} end";
		compiler.parse(src);
	}
	@Test(expected=DeliaException.class)
	public void testOptionalPrimaryKeyError() {
		DeliaCompiler compiler = initCompiler();
		String src = "type Point struct {x int primaryKey optional} end\n";
		compiler.parse(src);
	}
	@Test(expected=DeliaException.class)
	public void testUniquePrimaryKeyError() {
		DeliaCompiler compiler = initCompiler();
		String src = "type Point struct {x int primaryKey unique} end\n";
		compiler.parse(src);
	}
}
