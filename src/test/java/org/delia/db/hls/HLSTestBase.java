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
import org.delia.db.memdb.MemDBInterface;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.util.StringTrail;
import org.delia.zqueryresponse.LetSpan;
import org.delia.zqueryresponse.LetSpanEngine;
import org.delia.zqueryresponse.LetSpanRunner;

/**
 * 
 * 
 * @author Ian Rae
 *
 */
public class HLSTestBase extends NewBDDBase {
	
	protected HLSQueryStatement buildHLS(String src) {
		log.log(src);
		QueryExp queryExp = compileQuery(src);
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
		String initialSrc;
		if  (useCustomerSrc) {
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
		
		DeliaDao dao = createDao(); 
		boolean b = dao.initialize(initialSrc);
		assertEquals(true, b);

		Delia delia = dao.getDelia();
		this.session = dao.getMostRecentSession();
		ResultValue res = delia.continueExecution(src, session);
		
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		LetStatementExp letStatement = findLet(sessimpl);
		
		QueryExp queryExp = (QueryExp) letStatement.value;
		return queryExp;
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
	protected boolean useCustomerSrc = false;
	protected boolean useCustomer11Src = false;
	protected boolean useCustomer11OtherWaySrc = false;
	protected boolean useCustomer1NSrc = false;
	protected boolean useCustomer1NOtherWaySrc = false;
	
	protected DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		this.delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}

	protected String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
	protected String buildCustomerSrc() {
		String src = " type Customer struct {id int unique, x int, relation addr Address many optional  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  many optional } end";
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

}
