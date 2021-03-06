package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.ast.inputfunction.EndIfStatementExp;
import org.delia.compiler.ast.inputfunction.IdentPairExp;
import org.delia.compiler.ast.inputfunction.IfStatementExp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.compiler.ast.inputfunction.TLangBodyExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.compiler.parser.InputFunctionParser;
import org.delia.compiler.parser.NameAndFuncParser;
import org.delia.compiler.parser.TerminalParser;
import org.delia.dao.DeliaGenericDao;
import org.junit.Before;
import org.junit.Test;

public class InputFunctionParserTests extends InputFunctionTestBase {
	
	@Test
	public void test() {
		String src = "input function foo(Customer c) {}";
		InputFunctionDefStatementExp infnExp = parse(src);
		assertEquals("foo", infnExp.funcName);
		assertEquals(1, infnExp.argsL.size());
		chkArg(infnExp, 0, "Customer", "c");
	}
	
	@Test
	public void test2() {
		String src = "input function foo(Customer c, Address a) { field -> c.firstName, f2 -> c.z}";
		InputFunctionDefStatementExp infnExp = parse(src);
		assertEquals("foo", infnExp.funcName);
		assertEquals(2, infnExp.argsL.size());
		chkArg(infnExp, 0, "Customer", "c");
		chkArg(infnExp, 1, "Address", "a");
		
		assertEquals(2, infnExp.bodyExp.statementL.size());
		chkFnStatement(infnExp, 0, "field", "c", "firstName");
		TLangBodyExp texp = chkFnStatement(infnExp, 1, "f2", "c", "z").tlangBody;
		assertEquals(null, texp);
	}
	
	@Test
	public void test2Via() {
		String src = "input function foo(Customer c, Address a) { field -> c[z].firstName }";
		InputFunctionDefStatementExp infnExp = parse(src);
		assertEquals("foo", infnExp.funcName);
		assertEquals(2, infnExp.argsL.size());
		chkArg(infnExp, 0, "Customer", "c");
		chkArg(infnExp, 1, "Address", "a");
		
		assertEquals(1, infnExp.bodyExp.statementL.size());
		InputFuncMappingExp mappingExp = chkFnStatement(infnExp, 0, "field", "c", "firstName");
		assertEquals("z", mappingExp.outputViaTargetExp.strValue());
	}

	@Test
	public void testTLang() {
//		String src = "input function foo(Customer c, Address a) { field -> c.firstName using { x.y }, f2 -> c.z}";
		String src = "input function foo(Customer c, Address a) { field -> c.firstName using { x.y }}";
		InputFunctionDefStatementExp infnExp = parse(src);
		assertEquals("foo", infnExp.funcName);
		assertEquals(2, infnExp.argsL.size());
		chkArg(infnExp, 0, "Customer", "c");
		chkArg(infnExp, 1, "Address", "a");
		
		TLangBodyExp texp = chkFnStatement(infnExp, 0, "field", "c", "firstName").tlangBody;
		
		XNAFMultiExp multi = (XNAFMultiExp) texp.statementL.get(0);
		String name = getOne(multi);
		assertEquals("x", name);
		assertEquals("y", getSecond(multi));
	}
	
	private String getOne(Exp exp) {
		XNAFMultiExp x1 = (XNAFMultiExp) exp;
		return x1.qfeL.get(0).funcName;
	}
	private String getSecond(Exp exp) {
		XNAFMultiExp x1 = (XNAFMultiExp) exp;
		return x1.qfeL.get(1).funcName;
	}

	@Test
	public void testTLang2() {
		chkTLangInt("35", 35);
		Long bign = getLongNum();
		chkTLangLong(bign.toString(), getLongNum());
		Double dd = -1045.678;
		chkTLangDouble(dd.toString(), dd);
		
		chkTLangBoolean("true", true);
		chkTLangString("'abc'", "abc");
	}
	
	@Test
	public void testTLangIdent() {
		chkTLangIdent("x", "x", 0);
		chkTLangIdent("x.y(5)", "x", 1, "5");
		chkTLangIdent("x(5)", "x", 0, "5");
	}
	
	@Test
	public void testIf() {
		chkIf("if x then", "x");
		chkIf("elseif x then", "x");
	}
	@Test
	public void testEndIf() {
		chkEndIf("endif");
	}
	
	private void chkIf(String tlang, String expected) {
		TLangBodyExp texp = doTLang(tlang);
		
		IfStatementExp x1 = (IfStatementExp) texp.statementL.get(0);
		assertEquals(expected, x1.condition.strValue());
	}
	private void chkEndIf(String tlang) {
		TLangBodyExp texp = doTLang(tlang);
		
		EndIfStatementExp x1 = (EndIfStatementExp) texp.statementL.get(0);
	}

	private void chkTLangIdent(String tlang, String expected, int indexWithArgs, String...args) {
		TLangBodyExp texp = doTLang(tlang);
		
		XNAFMultiExp x1 = (XNAFMultiExp) texp.statementL.get(0);
		assertEquals(expected, x1.qfeL.get(0).funcName);
		if (args.length == 0) {
			chkStatementSize(texp, 1);
		} else {
			chkStatementSize(texp, 1);
			XNAFMultiExp x2 = (XNAFMultiExp) texp.statementL.get(0);
			XNAFSingleExp sexp = x2.qfeL.get(indexWithArgs);
			
			for(String arg: args) {
				assertEquals(arg, sexp.argL.get(0).strValue());
			}
		}
	}
	private void chkTLangString(String tlang, String expected) {
		TLangBodyExp texp = doTLang(tlang);
		
		StringExp x1 = (StringExp) texp.statementL.get(0);
		assertEquals(expected, x1.val);
		chkStatementSize(texp, 1);
	}
	private void chkTLangBoolean(String tlang, boolean expected) {
		TLangBodyExp texp = doTLang(tlang);
		
		BooleanExp x1 = (BooleanExp) texp.statementL.get(0);
		assertEquals(expected, x1.val);
		chkStatementSize(texp, 1);
	}
	private void chkTLangInt(String tlang, int expected) {
		TLangBodyExp texp = doTLang(tlang);
		
		IntegerExp x1 = (IntegerExp) texp.statementL.get(0);
		assertEquals(expected, x1.val.intValue());
		chkStatementSize(texp, 1);
	}
	private void chkTLangLong(String tlang, long expected) {
		TLangBodyExp texp = doTLang(tlang);
		
		LongExp x1 = (LongExp) texp.statementL.get(0);
		assertEquals(expected, x1.val.longValue());
		chkStatementSize(texp, 1);
	}
	private void chkTLangDouble(String tlang, double expected) {
		TLangBodyExp texp = doTLang(tlang);
		
		NumberExp x1 = (NumberExp) texp.statementL.get(0);
		assertEquals(expected, x1.val.doubleValue(), 0.0001);
		chkStatementSize(texp, 1);
	}
	private TLangBodyExp doTLang(String tlang) {
		String src = String.format("input function foo(Customer c, Address a) { field -> c.firstName using { %s }}", tlang);
		InputFunctionDefStatementExp infnExp = parse(src);
		TLangBodyExp texp = chkFnStatement(infnExp, 0, "field", "c", "firstName").tlangBody;
		return texp;
	}

	private void chkStatementSize(TLangBodyExp texp, int n) {
		assertEquals(n, texp.statementL.size());
	}

	private InputFuncMappingExp chkFnStatement(InputFunctionDefStatementExp infnExp, int i, String expected, String s2, String s3) {
		Exp z = infnExp.bodyExp.statementL.get(i);
		InputFuncMappingExp mappingExp = (InputFuncMappingExp) z;
		assertEquals(expected, mappingExp.getInputField());
		assertEquals(s2, mappingExp.outputField.val1);
		assertEquals(s3, mappingExp.outputField.val2);
		return mappingExp;
	}

	private void chkArg(InputFunctionDefStatementExp infnExp, int i, String expected, String expected2) {
		IdentPairExp pairExp = infnExp.argsL.get(i);
		assertEquals(expected, pairExp.typeName());
		assertEquals(expected2, pairExp.argName());
	}



	//----------
	@Before
	public void init() {
		DeliaGenericDao dao = this.createDao();
		this.delia = dao.getDelia();
		String src = buildSrc();
		this.session = delia.beginSession(src);
	}
	private String buildSrc() {
		String src = " type Customer struct {id int unique, wid int, name string } end";
		return src;
	}
	
	private InputFunctionDefStatementExp parse(String src) {
		log.log(src);
		NameAndFuncParser.initLazy();
		Exp exp = InputFunctionParser.inputFunction().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		return (InputFunctionDefStatementExp) exp;
	}
	
	private Long getLongNum() {
		int max = Integer.MAX_VALUE;
		long bigId = Long.valueOf((long)max) + 10; //2147483647
		return bigId;
	}
}
