package org.delia.mem;


import static org.junit.Assert.assertEquals;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.bdd.BDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.queryresponse.LetSpan;
import org.delia.queryresponse.LetSpanEngine;
import org.delia.queryresponse.LetSpanRunner;
import org.delia.queryresponse.LetSpanRunnerImpl;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.util.StringTrail;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;


public class LetSpanEngineTests extends BDDBase {
	
	@Test
	public void testRaw() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		Delia delia = dao.getDelia();
		src = "let x = Flight[true].orderBy('field1')";
		
		DeliaSession session = dao.getMostRecentSession();
		ResultValue res = delia.continueExecution(src, session);
		
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		LetStatementExp letStatement = findLet(sessimpl);
		
		LetSpanRunnerImpl spanRunner = new LetSpanRunnerImpl(delia.getFactoryService(), session.getExecutionContext().registry, null);
		LetSpanEngine letEngine = new LetSpanEngine(delia.getFactoryService(), session.getExecutionContext().registry);
		
		QueryExp queryExp = (QueryExp) letStatement.value;
		QueryResponse qresp = (QueryResponse) res.val;
		qresp = letEngine.process(queryExp, qresp, spanRunner);
		
		MyLetSpanRunner myrunner = new MyLetSpanRunner();
		letEngine = new LetSpanEngine(delia.getFactoryService(), session.getExecutionContext().registry);
		
		qresp = (QueryResponse) res.val;
		qresp = letEngine.process(queryExp, qresp, myrunner);
		assertEquals("Flight;orderBy(field1)", myrunner.trail.getTrail());
	}
	
	@Test
	public void test1() {
		chkRun("let x = Flight[true]", "");
		chkRun("let x = Flight[true].orderBy('field1')", "Flight;orderBy(field1)");
		
		chkRun("let x = Flight[true].orderBy('field1').field2", "Flight;orderBy(field1);field2");
//		chkRun("let x = Flight[true].field2.orderBy('field1')", "Flight;orderBy(field1);field2");
//		chkRun("let x = Flight[true].orderBy('field1')", "Flight;orderBy(field1)");
	}
	
	

	private void chkRun(String src, String expected) {
		String initialSrc = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(initialSrc);
		assertEquals(true, b);

		Delia delia = dao.getDelia();
		DeliaSession session = dao.getMostRecentSession();
		ResultValue res = delia.continueExecution(src, session);
		
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		LetStatementExp letStatement = findLet(sessimpl);
		
		QueryExp queryExp = (QueryExp) letStatement.value;
		QueryResponse qresp = (QueryResponse) res.val;
		
		MyLetSpanRunner myrunner = new MyLetSpanRunner();
		LetSpanEngine letEngine = new LetSpanEngine(delia.getFactoryService(), session.getExecutionContext().registry);
		
		qresp = (QueryResponse) res.val;
		qresp = letEngine.process(queryExp, qresp, myrunner);
		assertEquals(expected, myrunner.trail.getTrail());
	}

	private LetStatementExp findLet(DeliaSession session) {
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		for(Exp exp: sessimpl.mostRecentContinueExpL) {
			if (exp instanceof LetStatementExp) {
				return (LetStatementExp) exp;
			}
		}
		return null;
	}

	public static class MyLetSpanRunner implements LetSpanRunner {

		private StringTrail trail = new StringTrail();

		@Override
		public QueryResponse executeSpan(LetSpan span) {
			trail.add(span.dtype.getName());
			for(QueryFuncExp qfe: span.qfeL) {
				String s = qfe.strValue();
				trail.add(s);
			}
			return span.qresp;
		}
	}


	//---

	@Before
	public void init() {
	}

	private DeliaGenericDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaGenericDao(delia);
	}

	private String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}

	@Override
	public ZDBInterfaceFactory createForTest() {
		MemZDBInterfaceFactory db = new MemZDBInterfaceFactory(createFactorySvc());
		return db;
	}

}
