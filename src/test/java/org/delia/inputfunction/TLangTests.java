package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.memdb.filter.OP;
import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;
import org.delia.tlang.runner.BasicCondition;
import org.delia.tlang.runner.DValueOpEvaluator;
import org.delia.tlang.runner.IsMissingCondition;
import org.delia.tlang.runner.OpCondition;
import org.delia.tlang.runner.TLangContext;
import org.delia.tlang.runner.TLangProgram;
import org.delia.tlang.runner.TLangResult;
import org.delia.tlang.runner.TLangRunner;
import org.delia.tlang.statement.EndIfStatement;
import org.delia.tlang.statement.IfStatement;
import org.delia.tlang.statement.TLangStatementBase;
import org.delia.tlang.statement.ToUpperStatement;
import org.delia.tlang.statement.ValueStatement;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;

public class TLangTests  extends NewBDDBase {
	public static class AddXStatement extends TLangStatementBase {
		public AddXStatement() {
			super("addX");
		}
		@Override
		public void execute(DValue value, TLangResult result, TLangContext ctx) {
			String s = value.asString();
			s += "X";
			result.val = ctx.builder.buildString(s);
		}
	}
	@Test
	public void test1() {
		TLangRunner inFuncRunner = createXConv();
		TLangProgram prog = createProgram();

		DValue initialValue = builder.buildString("abc");
		TLangResult res = inFuncRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("ABCX", dval.asString());
		chkTrail(inFuncRunner, "toUpperCase;addX");
	}

	@Test
	public void test2() {
		TLangRunner inFuncRunner = createXConv();
		TLangProgram prog = createProgram2(false);

		DValue initialValue = builder.buildString("abc");
		TLangResult res = inFuncRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("abcX", dval.asString());
		chkTrail(inFuncRunner, "if;endif;addX");
	}
	@Test
	public void test2a() {
		TLangRunner inFuncRunner = createXConv();
		TLangProgram prog = createProgram2(true);

		DValue initialValue = builder.buildString("abc");
		TLangResult res = inFuncRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("ABCX", dval.asString());
		chkTrail(inFuncRunner, "if;toUpperCase;endif;addX");
	}
	@Test
	public void test3() {
		TLangRunner inFuncRunner = createXConv();
		TLangProgram prog = createProgram3(false);

		DValue initialValue = builder.buildString("");
		TLangResult res = inFuncRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("Z", dval.asString());
		chkTrail(inFuncRunner, "if;value;endif");
	}
	@Test
	public void test3a() {
		TLangRunner inFuncRunner = createXConv();
		TLangProgram prog = createProgram3(true);

		DValue initialValue = builder.buildString("");
		TLangResult res = inFuncRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("ZX", dval.asString());
		chkTrail(inFuncRunner, "if;value;endif;addX");
	}
	@Test
	public void test4IfTrue() {
		TLangRunner inFuncRunner = createXConv();
		TLangProgram prog = createProgram4(false);

		DValue initialValue = builder.buildString("abc");
		TLangResult res = inFuncRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("Z", dval.asString());
		chkTrail(inFuncRunner, "if;value;endif");
	}
	@Test
	public void test4IfFalse() {
		TLangRunner inFuncRunner = createXConv();
		TLangProgram prog = createProgram4(false);

		DValue initialValue = builder.buildString("something");
		TLangResult res = inFuncRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("something", dval.asString());
		chkTrail(inFuncRunner, "if;endif");
	}

	// --
	//	private DeliaDao dao;
	private Delia delia;
	private DeliaSession session;
	private DTypeRegistry registry;
	ScalarValueBuilder builder;

	@Before
	public void init() {
		DeliaDao dao = this.createDao();
		this.delia = dao.getDelia();
		String src = buildSrc();
		this.session = delia.beginSession(src);
		this.registry = session.getExecutionContext().registry;
		this.builder = delia.getFactoryService().createScalarValueBuilder(registry);
	}
	private String buildSrc() {
		String src = " type Customer struct {id int unique, wid int, name string } end";
		src += " input function foo(Customer c) { ID -> c.id, WID -> c.wid, NAME -> c.name}";

		return src;
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}
	private TLangProgram createProgram() {
		TLangProgram prog = new TLangProgram();
		prog.statements.add(new ToUpperStatement());
		prog.statements.add(new AddXStatement());
		return prog;
	}
	private TLangProgram createProgram2(boolean bb) {
		TLangProgram prog = new TLangProgram();
		prog.statements.add(new IfStatement(new BasicCondition(bb)));
		prog.statements.add(new ToUpperStatement());
		prog.statements.add(new EndIfStatement());
		prog.statements.add(new AddXStatement());
		return prog;
	}
	private TLangProgram createProgram3(boolean bb) {
		TLangProgram prog = new TLangProgram();
		prog.statements.add(new IfStatement(new IsMissingCondition()));

		DValue x = builder.buildString("Z");
		prog.statements.add(new ValueStatement(x));
		prog.statements.add(new EndIfStatement());
		if (bb) {
			prog.statements.add(new AddXStatement());
		}
		return prog;
	}
	private TLangProgram createProgram4(boolean bb) {
		TLangProgram prog = new TLangProgram();
		
		DValueOpEvaluator eval = new DValueOpEvaluator(OP.EQ);
		OpCondition cond = new OpCondition(eval);
		DValue xx = builder.buildString("abc");
		eval.setRightVar(xx);
		prog.statements.add(new IfStatement(cond));

		DValue x = builder.buildString("Z");
		prog.statements.add(new ValueStatement(x));
		prog.statements.add(new EndIfStatement());
		if (bb) {
			prog.statements.add(new AddXStatement());
		}
		return prog;
	}

	private void chkNoErrors(List<DeliaError> totalErrorL) {
		for(DeliaError err: totalErrorL) {
			delia.getLog().log("err: %s", err.toString());
		}
		assertEquals(0, totalErrorL.size());
	}

	private TLangRunner createXConv() {
		return new TLangRunner(delia.getFactoryService(), registry);
	}


	private void chkTrail(TLangRunner inFuncRunner, String expected) {
		delia.getLog().log(inFuncRunner.trail.getTrail());
		assertEquals(expected, inFuncRunner.trail.getTrail());
	}



	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}


}
