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
		void execute(DValue value, TLangResult result, TLangContext ctx);
	}
	
	public static class TLangProgram {
		List<TLangStatement> statements = new ArrayList<>();
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
			for(TLangStatement statement: program.statements) {
				TLangContext ctx = new TLangContext();
				ctx.builder = scalarBuilder;
				res.ok = true;
				statement.execute(dval, res, ctx);
				if (! res.ok) {
					break;
				}
			}
			
			
			TLangResult result = res;
			result.ok = true;
			result.val = dval;
			return result;
		}
	}
	
	public static class ToUpperStatement implements TLangStatement {

		@Override
		public void execute(DValue value, TLangResult result, TLangContext ctx) {
			String s = value.asString();
			s = s.toUpperCase();
			DValue x = ctx.builder.buildString(s);
			
			result.val = x;
		}
		
	}
	
	@Test
	public void test2() {
		TLangRunner inFuncRunner = createXConv();
		
		TLangProgram prog = createProgram();
		
		DValue initialValue = builder.buildString("abc");
		TLangResult res = inFuncRunner.execute(prog, initialValue);

		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals("ABC", dval.asString());
	}

	private TLangProgram createProgram() {
		TLangProgram prog = new TLangProgram();
		prog.statements.add(new ToUpperStatement());
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
