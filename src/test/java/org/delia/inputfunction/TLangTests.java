package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.bdd.BDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaDao;
import org.delia.db.DBType;
import org.delia.db.memdb.filter.OP;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.runner.VarEvaluator;
import org.delia.tlang.runner.BasicCondition;
import org.delia.tlang.runner.DValueOpEvaluator;
import org.delia.tlang.runner.IsMissingCondition;
import org.delia.tlang.runner.OpCondition;
import org.delia.tlang.runner.TLangContext;
import org.delia.tlang.runner.TLangProgram;
import org.delia.tlang.runner.TLangResult;
import org.delia.tlang.runner.TLangRunner;
import org.delia.tlang.runner.TLangRunnerImpl;
import org.delia.tlang.runner.TLangVarEvaluator;
import org.delia.tlang.statement.EndIfStatement;
import org.delia.tlang.statement.IfStatement;
import org.delia.tlang.statement.TLangStatementBase;
import org.delia.tlang.statement.ToUpperStatement;
import org.delia.tlang.statement.ValueStatement;
import org.delia.tlang.statement.VariableStatement;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;

public class TLangTests  extends BDDBase {
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
		TLangRunner tlangRunner = createTLangRunner();
		TLangProgram prog = createProgram();

		DValue initialValue = builder.buildString("abc");
		TLangResult res = tlangRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("ABCX", dval.asString());
		chkTrail(tlangRunner, "toUpperCase;addX");
	}

	@Test
	public void test2() {
		TLangRunner tlangRunner = createTLangRunner();
		TLangProgram prog = createProgram2(false);

		DValue initialValue = builder.buildString("abc");
		TLangResult res = tlangRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("abcX", dval.asString());
		chkTrail(tlangRunner, "if;endif;addX");
	}
	@Test
	public void test2a() {
		TLangRunner tlangRunner = createTLangRunner();
		TLangProgram prog = createProgram2(true);

		DValue initialValue = builder.buildString("abc");
		TLangResult res = tlangRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("ABCX", dval.asString());
		chkTrail(tlangRunner, "if;toUpperCase;endif;addX");
	}
	@Test
	public void test3() {
		TLangRunner tlangRunner = createTLangRunner();
		TLangProgram prog = createProgram3(false);

		DValue initialValue = builder.buildString("");
		TLangResult res = tlangRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("Z", dval.asString());
		chkTrail(tlangRunner, "if;value;endif");
	}
	@Test
	public void test3a() {
		TLangRunner tlangRunner = createTLangRunner();
		TLangProgram prog = createProgram3(true);

		DValue initialValue = builder.buildString("");
		TLangResult res = tlangRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("ZX", dval.asString());
		chkTrail(tlangRunner, "if;value;endif;addX");
	}
	@Test
	public void test4IfTrue() {
		TLangRunner tlangRunner = createTLangRunner();
		TLangProgram prog = createProgram4(false);

		DValue initialValue = builder.buildString("abc");
		TLangResult res = tlangRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("Z", dval.asString());
		chkTrail(tlangRunner, "if;value;endif");
	}
	@Test
	public void test4IfFalse() {
		TLangRunner tlangRunner = createTLangRunner();
		TLangProgram prog = createProgram4(false);

		DValue initialValue = builder.buildString("something");
		TLangResult res = tlangRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("something", dval.asString());
		chkTrail(tlangRunner, "if;endif");
	}
	@Test
	public void test5Var() {
		TLangRunner tlangRunner = createTLangRunner();
		TLangProgram prog = createProgram5(false);

		DValue initialValue = builder.buildString("something");

		buildVar("z", "abcd");
		
		VarEvaluator varEvaluator = new TLangVarEvaluator(session.getExecutionContext());
		tlangRunner.setVarEvaluator(varEvaluator);
		TLangResult res = tlangRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("abcd", dval.asString());
		chkTrail(tlangRunner, "var");
	}

	private void buildVar(String varName, String str) {
		DValue varvalue = builder.buildString(str);
		QueryResponse qresp = new QueryResponse();
		qresp.ok = true;
		qresp.dvalList = Collections.singletonList(varvalue);
		ResultValue res = new ResultValue();
		res.ok = true;
		res.val = qresp;
		session.getExecutionContext().varMap.put(varName, res);
	}

	// --
	//	private DeliaDao dao;
	private Delia delia;
	private DeliaSession session;
	private DTypeRegistry registry;
	private ScalarValueBuilder builder;
	
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
		prog.statements.add(new ToUpperStatement(true));
		prog.statements.add(new AddXStatement());
		return prog;
	}
	private TLangProgram createProgram2(boolean bb) {
		TLangProgram prog = new TLangProgram();
		prog.statements.add(new IfStatement(new BasicCondition(bb), false));
		prog.statements.add(new ToUpperStatement(true));
		prog.statements.add(new EndIfStatement());
		prog.statements.add(new AddXStatement());
		return prog;
	}
	private TLangProgram createProgram3(boolean bb) {
		TLangProgram prog = new TLangProgram();
		prog.statements.add(new IfStatement(new IsMissingCondition(), false));

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
		prog.statements.add(new IfStatement(cond, false));

		DValue x = builder.buildString("Z");
		prog.statements.add(new ValueStatement(x));
		prog.statements.add(new EndIfStatement());
		if (bb) {
			prog.statements.add(new AddXStatement());
		}
		return prog;
	}
	
	private TLangProgram createProgram5(boolean bb) {
		TLangProgram prog = new TLangProgram();
		
		prog.statements.add(new VariableStatement("z"));
		return prog;
	}

	private TLangRunner createTLangRunner() {
		return new TLangRunnerImpl(delia.getFactoryService(), registry);
	}
	private void chkTrail(TLangRunner tlangRunner, String expected) {
		delia.getLog().log(tlangRunner.getTrail());
		assertEquals(expected, tlangRunner.getTrail());
	}


	@Override
	public ZDBInterfaceFactory createForTest() {
		MemZDBInterfaceFactory db = new MemZDBInterfaceFactory(createFactorySvc());
		return db;
	}


}
