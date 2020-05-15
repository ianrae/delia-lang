package org.delia.db.hls;


import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.SchemaContext;
import org.delia.db.TableExistenceService;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.sql.fragment.MiniSelectFragmentParser;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.queryresponse.LetSpan;
import org.delia.queryresponse.LetSpanEngine;
import org.delia.queryresponse.LetSpanRunner;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.StringTrail;

/**
 * 
 * 
 * @author Ian Rae
 *
 */
public class HLSTestBase extends NewBDDBase {
	
	protected HLSQueryStatement buildHLS(String src) {
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		LetSpanEngine letEngine = new LetSpanEngine(delia.getFactoryService(), session.getExecutionContext().registry, null, null);
		List<LetSpan> spanL = letEngine.buildAllSpans(queryExp);
		
		HLSEngine hlsEngine = new HLSEngine(delia.getFactoryService(), session.getExecutionContext().registry);
		HLSQueryStatement hls = hlsEngine.generateStatement(queryExp, spanL);
		
		for(HLSQuerySpan hlspan: hls.hlspanL) {
			String hlstr = hlspan.toString();
			log.log(hlstr);
		}
		return hls;
	}


	protected QueryExp compileQuery(String src) {
		LetStatementExp letStatement = compileQueryToLetStatement(src);
		
		QueryExp queryExp = (QueryExp) letStatement.value;
		return queryExp;
	}
	
	protected LetStatementExp compileQueryToLetStatement(String src) {
		String initialSrc;
		if  (useCustomerManyToManySrc) {
			initialSrc = buildCustomerSrc();
		} else if (useCustomer11Src) {
			initialSrc = buildCustomer11Src();
		} else if (useCustomer11OtherWaySrc) {
			initialSrc = buildCustomer11OtherWasySrc();
		} else if (useCustomer1NSrc) {
			initialSrc = buildCustomer1NSrc();
		} else if (useCustomer1NOtherWaySrc) {
			initialSrc = buildCustomer1NOtherWaySrc();
		} else {
			initialSrc = buildSrc();
		}
		log.log("initial: " + initialSrc);
		
		DeliaDao dao = createDao(); 
		boolean b = dao.initialize(initialSrc);
		assertEquals(true, b);

		Delia delia = dao.getDelia();
		this.session = dao.getMostRecentSession();
		ResultValue res = delia.continueExecution(src, session);
		
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		LetStatementExp letStatement = findLet(sessimpl);
		return letStatement;
	}

	protected LetStatementExp findLet(DeliaSession session) {
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		for(Exp exp: sessimpl.mostRecentContinueExpL) {
			if (exp instanceof LetStatementExp) {
				return (LetStatementExp) exp;
			}
		}
		return null;
	}

	public static class MyLetSpanRunner implements LetSpanRunner {

		protected StringTrail trail = new StringTrail();

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
	protected Delia delia;
	protected DeliaSession session;
	protected boolean useCustomerManyToManySrc = false;
	protected boolean useCustomer11Src = false;
	protected boolean useCustomer11OtherWaySrc = false;
	protected boolean useCustomer1NSrc = false;
	protected boolean useCustomer1NOtherWaySrc = false;
	protected boolean insertSomeRecords = false;
	protected boolean flipAssocTbl = false; //mosts tests assume CustomerAddressAssoc
	
	//---
	protected TableExistenceService existsSvc;
	protected AssocTblManager assocTblMgr;
	
	protected DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		this.delia = DeliaBuilder.withConnection(info).build();
		SchemaContext ctx = new SchemaContext();
		
		if (flipAssocTbl) {
			delia.getDBInterface().createTable("AddressCustomerAssoc", null, ctx);
		} else {
			delia.getDBInterface().createTable("CustomerAddressAssoc", null, ctx);
		}
		existsSvc = new TableExistenceServiceImpl(delia.getDBInterface(), null); //2nd param not needed for MEM
		assocTblMgr = new AssocTblManager(existsSvc);
		return new DeliaDao(delia);
	}

	protected String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
	protected String buildCustomerSrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address many optional  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  many optional } end";
		
		if (insertSomeRecords) {
			src += "\n insert Customer {cid:55, x:10}";
			src += "\n insert Customer {cid:56, x:11}";
			src += "\n insert Address {id:100, y:20, cust:55}";
			src += "\n insert Address {id:101, y:20, cust:55}";
		}
		return src;
	}
	protected String buildCustomer11Src() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address one optional parent  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  one optional } end";
		return src;
	}
	protected String buildCustomer11OtherWasySrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address one optional   } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  one optional parent} end";
		return src;
	}
	protected String buildCustomer1NSrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address many optional  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  one optional } end";
		return src;
	}
	protected String buildCustomer1NOtherWaySrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address one optional  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer many optional } end";
		return src;
	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}
	
	protected HLSSQLGenerator createGen() {
		DTypeRegistry registry = session.getExecutionContext().registry;
		WhereFragmentGenerator whereGen = new WhereFragmentGenerator(delia.getFactoryService(), registry, null);
		MiniSelectFragmentParser mini = new MiniSelectFragmentParser(delia.getFactoryService(), registry, whereGen);
		HLSSQLGenerator gen = new HLSSQLGeneratorImpl(delia.getFactoryService(), assocTblMgr, mini, null, existsSvc);
		gen.setRegistry(session.getExecutionContext().registry);
		return gen;
	}
	
	protected void doSqlchkP(String src, String sqlExpected, String param1) {
		doSqlchkPR(src, sqlExpected, param1, null);
	}

	private void doSqlchkPR(String src, String sqlExpected, String param1, String rendered) {
		HLSQueryStatement hls = buildHLS(src);
		HLSSQLGenerator gen = createGen();
		String sql = gen.buildSQL(hls);
		log.log("sql: " + sql);
		assertEquals(sqlExpected, sql);
		
		HLSQuerySpan hlspan = hls.getMainHLSSpan();
		if (param1 != null) {
			assertEquals(1, hlspan.paramL.size());
			DValue dval = hlspan.paramL.get(0);
			assertEquals(param1, dval.asString());
		} else {
			assertEquals(0, hlspan.paramL.size());
		}
		
		logRenderRFList(hls);
	}


	private void logRenderRFList(HLSQueryStatement hls) {
		RenderedFieldHelper.logRenderedFieldList(hls, log);
		List<RenderedField> list = hls.getRenderedFields();
		
		for(RenderedField rf: list) {
			if (rf.field.contains("*") || rf.field.contains("(")) {
			} else {
				assertEquals(true, rf.isAssocField || rf.structType != null || rf.field.equals("*"));
			}
		}
	}
}
