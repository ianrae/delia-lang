package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.memdb.filter.OP;
import org.delia.error.DeliaError;
import org.delia.log.LogLevel;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;

public class TLangTests  extends NewBDDBase {
	public static class TLangResult {
		public boolean ok;
		public Object val;
	}
	
	public static class TLangContext {
		public ScalarValueBuilder builder;
	}
	public interface TLangStatement {
		boolean evalCondition(DValue dval);
		void execute(DValue value, TLangResult result, TLangContext ctx);
	}
	
	public static class TLangProgram {
		List<TLangStatement> statements = new ArrayList<>();
	}
	
	
	public interface Condition {
		boolean eval(DValue dval);
	}
	public static class BasicCondition implements Condition {
		public boolean bb;
		
		public BasicCondition(boolean b) {
			this.bb = b;
		}
		@Override
		public boolean eval(DValue dval) {
			return bb;
		}
	}
	
	public static class IsMissingCondition implements Condition {
		
		public IsMissingCondition() {
		}
		@Override
		public boolean eval(DValue dval) {
			return dval == null || dval.asString().isEmpty();
		}
	}
	public static class OpCondition implements Condition {
		public OP op;
		
		public OpCondition(OP op) {
		}
		@Override
		public boolean eval(DValue dval) {
			return dval == null || dval.asString().isEmpty();
		}
	}
	
	
	public static class IfStatement implements TLangStatement {
		public Condition cond;
		
		public IfStatement(Condition cond) {
			this.cond = cond;
		}
		@Override
		public void execute(DValue value, TLangResult result, TLangContext ctx) {
			result.val = value;
		}
		@Override
		public boolean evalCondition(DValue dval) {
			return cond.eval(dval);
		}
	}
	public static class ElseIfStatement implements TLangStatement {
		public Condition cond;
		public ElseIfStatement(Condition cond) {
			this.cond = cond;
		}
		@Override
		public void execute(DValue value, TLangResult result, TLangContext ctx) {
			result.val = value;
		}
		@Override
		public boolean evalCondition(DValue dval) {
			return cond.eval(dval);
		}
	}
	public static class EndIfStatement implements TLangStatement {
		@Override
		public void execute(DValue value, TLangResult result, TLangContext ctx) {
			result.val = value;
		}
		@Override
		public boolean evalCondition(DValue dval) {
			return true;
		}
	}

	
	public static class TLangRunner extends ServiceBase {

		private DTypeRegistry registry;
		private ScalarValueBuilder scalarBuilder;

		public TLangRunner(FactoryService factorySvc, DTypeRegistry registry) {
			super(factorySvc);
			this.registry = registry;
			this.scalarBuilder = factorySvc.createScalarValueBuilder(registry);
		}
		
		public TLangResult execute(TLangProgram program, DValue initialValue) {
			
			DValue dval = initialValue;
			TLangResult res = new TLangResult();
			int ipIndex;
			for(ipIndex = 0; ipIndex < program.statements.size(); ipIndex++) {
				TLangStatement statement = program.statements.get(ipIndex);
				if (statement.evalCondition(dval)) {
					TLangContext ctx = new TLangContext();
					ctx.builder = scalarBuilder;
					res.ok = true;
					statement.execute(dval, res, ctx);
					if (! res.ok) {
						break;
					}
					dval = (DValue) res.val;
				} else {
					ipIndex = findNext(program, ipIndex);
					if (ipIndex < 0) {
						//err missing endif
					}
				}
			}
			
			TLangResult result = res;
			result.ok = true;
			result.val = dval;
			return result;
		}

		private int findNext(TLangProgram program, int ipIndexCurrent) {
			for(int ipIndex = ipIndexCurrent + 1; ipIndex < program.statements.size(); ipIndex++) {
				TLangStatement statement = program.statements.get(ipIndex);
				if (statement instanceof EndIfStatement || statement instanceof ElseIfStatement) {
					return ipIndex;
				}
			}
			return -1;
		}
	}
	
	public static abstract class TLangStatementBase implements TLangStatement {
		@Override
		public abstract void execute(DValue value, TLangResult result, TLangContext ctx);
		@Override
		public boolean evalCondition(DValue dval) {
			return true;
		}
	}
	public static class ToUpperStatement extends TLangStatementBase {
		@Override
		public void execute(DValue value, TLangResult result, TLangContext ctx) {
			String s = value.asString();
			s = s.toUpperCase();
			result.val = ctx.builder.buildString(s);
		}
	}
	public static class AddXStatement extends TLangStatementBase {
		@Override
		public void execute(DValue value, TLangResult result, TLangContext ctx) {
			String s = value.asString();
			s += "X";
			result.val = ctx.builder.buildString(s);
		}
	}
	public static class ValueStatement extends TLangStatementBase {
		private DValue dval;
		public ValueStatement(DValue dval) {
			this.dval = dval;
		}
		@Override
		public void execute(DValue value, TLangResult result, TLangContext ctx) {
			result.val = dval;
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

	private void chkNoErrors(List<DeliaError> totalErrorL) {
		for(DeliaError err: totalErrorL) {
			delia.getLog().log("err: %s", err.toString());
		}
		assertEquals(0, totalErrorL.size());
	}

	private TLangRunner createXConv() {
		return new TLangRunner(delia.getFactoryService(), registry);
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


	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}
	
	
}
