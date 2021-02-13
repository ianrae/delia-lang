package org.delia.bdd.core;

import static org.junit.Assert.assertEquals;

import org.delia.base.UnitTestLog;
import org.delia.bdd.old.BDDQueryHelper;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.db.DBType;
import org.delia.db.h2.DeliaInitializer;
import org.delia.log.Log;
import org.delia.runner.CompilerHelper;
import org.delia.runner.LegacyRunner;
import org.delia.runner.ResultValue;
import org.delia.runner.Runner;
import org.delia.runner.RunnerHelper;
import org.delia.type.DValue;
import org.delia.type.Shape;

public class BDDTester {
	private Log log = new UnitTestLog();
	protected RunnerHelper helper = new RunnerHelper();
	protected CompilerHelper chelper = new CompilerHelper(null);
	protected DeliaInitializer initter;
	protected LegacyRunner runner;
	protected BDDQueryHelper qhelper;
	
	public BDDTester() {
		initter = new DeliaInitializer();
		runner = initter.init(DBType.MEM, log);
		qhelper = new BDDQueryHelper(initter, runner.innerRunner);
	}

	public boolean chkString(String delia, String expected) {
		ResultValue res = doLet(delia, "string");
		assertEquals(Shape.STRING, res.shape);
		DValue dval = res.getAsDValue();
		return compareString(expected, dval.asString());
	}
	private boolean compareString(String expected, String s) {
		if (expected == null && s == null) {
			return true;
		} else {
			return expected.equals(s);
		}
	}
	private boolean compareInt(Integer expected, Integer s) {
		if (expected == null && s == null) {
			return true;
		} else {
			return expected.equals(s);
		}
	}

	public boolean chkInt(String delia, String expected) {
		ResultValue res = doLet(delia, "int");
		assertEquals(Shape.INTEGER, res.shape);
		DValue dval = res.getAsDValue();
		Integer n = Integer.parseInt(expected);
		return compareInt(n, dval.asInt());
	}

	private int nextVarNum =1;
	private ResultValue doLet(String src, String typeName) {
		CompilerHelper chelper = new CompilerHelper(null);
		LetStatementExp exp = (LetStatementExp) chelper.parseOne(src);
		assertEquals(typeName, exp.typeName);

		ResultValue res = runner.executeOneStatement(exp);
		assertEquals(true, res.ok);
		return res;
	}
}