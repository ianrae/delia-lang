package org.delia.runner;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.base.UnitTestLog;
import org.delia.compiler.DeliaCompiler;
import org.delia.compiler.ast.ConfigureStatementExp;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.compiler.ast.UserFunctionDefStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.log.SimpleLog;
import org.delia.runner.InternalCompileState;
import org.delia.runner.QueryResponse;



public class CompilerHelper {
	

	private InternalCompileState execCtx;
	private Log preExistingLog;
	
	public CompilerHelper(InternalCompileState execCtx) {
		this(execCtx, null);
	}
	public CompilerHelper(InternalCompileState execCtx, Log log) {
		this.execCtx = execCtx;
		this.preExistingLog = log;
	}

	public void setPreExistingLog(Log log) {
		this.preExistingLog = log;
	}
	public DeliaCompiler initCompiler()  {
		Log log = preExistingLog == null ? new UnitTestLog() : preExistingLog;
		ErrorTracker et = new SimpleErrorTracker(log);
		FactoryService factorySvc = new FactoryServiceImpl(log, et);
		DeliaCompiler compiler = new DeliaCompiler(factorySvc, execCtx);
		return compiler;
	}

	public Exp parseOne(String input) {
		DeliaCompiler compiler = initCompiler();
		List<Exp> result = compiler.parse(input);
		assertEquals(1, result.size());
		return result.get(0);
	}
	public List<Exp> parseTwo(String input) {
		initCompiler();
		List<Exp> list = parse(input);
		assertEquals(2, list.size());
		return list;
	}
	public List<Exp> parse(String input) {
		DeliaCompiler compiler = initCompiler();
		List<Exp> result = compiler.parse(input);
		return result;
	}

	public LetStatementExp chkBoolean(String input, String output) {
		LetStatementExp exp = (LetStatementExp) parseOne(input);
		assertEquals(output, exp.toString());
		assertEquals("boolean", exp.typeName);
		return exp;
	}
	public LetStatementExp chkString(String input, String output) {
		LetStatementExp exp = (LetStatementExp) parseOne(input);
		assertEquals(output, exp.toString());
		assertEquals("string", exp.typeName);
		return exp;
	}
	public UserFunctionDefStatementExp chkUserFn(String input, String fnName) {
		UserFunctionDefStatementExp exp = (UserFunctionDefStatementExp) parseOne(input);
		assertEquals(fnName, exp.funcName);
		return exp;
	}
	public TypeStatementExp chkType(String input, String output) {
		TypeStatementExp exp = (TypeStatementExp) parseOne(input);
		if (output != null) {
			assertEquals(output, exp.toString());
		}
		return exp;
	}
	public UpdateStatementExp chkUpdate(String input, String output) {
		UpdateStatementExp exp = (UpdateStatementExp) parseOne(input);
		if (output != null) {
			assertEquals(output, exp.toString());
		}
		return exp;
	}
	public LetStatementExp chkScalarLet(String input, String type) {
		LetStatementExp exp = (LetStatementExp) parseOne(input);
		assertEquals(type, exp.typeName);
		return exp;
	}
	public LetStatementExp chkUserFuncInvoke(String input, String output) {
		LetStatementExp exp = (LetStatementExp) parseOne(input);
		if (output != null)
		{
			assertEquals(output, exp.toString());
		}
		assertEquals("userFunc", exp.typeName);
		return exp;
	}
	public InsertStatementExp chkInsert(String input, String output) {
		InsertStatementExp exp = (InsertStatementExp) parseOne(input);
		if (output != null) {
			assertEquals(output, exp.toString());
		}
		return exp;
	}
	public LetStatementExp chkQueryLet(String input, String output) {
		LetStatementExp exp = (LetStatementExp) parseOne(input);
		if (output != null)
		{
			assertEquals(output, exp.toString());
		}
		assertEquals("queryResponse", exp.typeName);
		return exp;
	}
	public DeleteStatementExp chkDelete(String input, String output) {
		DeleteStatementExp exp = (DeleteStatementExp) parseOne(input);
		if (output != null) {
			assertEquals(output, exp.toString());
		}
		return exp;
	}
	public QueryResponse chkResQueryEmpty(QueryResponse qresp) {
		assertEquals(true, qresp.ok);
		assertEquals(true, qresp.dvalList.isEmpty());
		return qresp;
	}
	public ConfigureStatementExp chkConfingure(String input) {
		ConfigureStatementExp exp = (ConfigureStatementExp) parseOne(input);
		return exp;
	}

}
