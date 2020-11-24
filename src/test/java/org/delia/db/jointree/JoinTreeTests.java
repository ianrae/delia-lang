package org.delia.db.jointree;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.hls.HLSTestBase;
import org.delia.queryresponse.LetSpan;
import org.delia.queryresponse.LetSpanEngine;
import org.delia.queryresponse.function.ZQueryResponseFunctionFactory;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.junit.Before;
import org.junit.Test;

/**
 * JoinTree extracts all potential JOINS from a query
 * 
 * 
 * @author Ian Rae
 *
 */
public class JoinTreeTests extends HLSTestBase {
	
	public static class JTElement  {
		public DStructType dtype;
		public String fieldName;
		public DStructType fieldType;
		public List<JTElement> nextL = new ArrayList<>();
		
		
		@Override
		public String toString() {
			StringJoiner joiner = new StringJoiner("|");
			joiner.add(dtype.getName());
			joiner.add(fieldName);
			joiner.add(fieldType.getName());
			return joiner.toString();
		}
	}
	
	
	public static class JoinTreeEngine extends ServiceBase {
		private DTypeRegistry registry;
		private ZQueryResponseFunctionFactory fnFactory;

		public JoinTreeEngine(FactoryService factorySvc, DTypeRegistry registry) {
			super(factorySvc);
			this.registry = registry;
			
			this.fnFactory = new ZQueryResponseFunctionFactory(factorySvc, null); //fetchRunner not needed here
		}
		
		public List<JTElement> parse(QueryExp queryExp, List<LetSpan> spanL) {
			List<JTElement> resultL = new ArrayList<>();
			for(LetSpan span: spanL) {
				if (!span.qfeL.isEmpty()) {
					QueryFuncExp qfe = span.qfeL.get(0);
					if (qfe.funcName.equals("fks")) {
						addFKs(span, resultL);
					}
				}
			}
			return resultL;
		}
		
		
		private void addFKs(LetSpan span, List<JTElement> resultL) {
			DStructType structType = (DStructType) span.dtype;
			for(TypePair pair: structType.getAllFields()) {
				if (pair.type.isStructShape()) {
					RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
					if (relinfo.isParent || RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
						JTElement el = new JTElement();
						el.dtype = (DStructType) span.dtype;
						el.fieldName = pair.name;
						el.fieldType = (DStructType) pair.type;
						resultL.add(el);
					}
				}
			}
			
		}
	}	
	
	

	@Test
	public void test() {
		useCustomer11Src = true;
		parseIntoLetSpans("let x = Customer[55].fks()", 1, "Customer|addr|Address"); //, 					"SELECT a.cid,a.x,b.id as addr FROM Customer as a LEFT JOIN Address as b ON a.cid=b.cust WHERE a.cid = ?", "55");
		
		
		
//		sqlchkP("let x = Customer[55].fks()", 					"SELECT a.cid,a.x,b.id as addr FROM Customer as a LEFT JOIN Address as b ON a.cid=b.cust WHERE a.cid = ?", "55");
//		sqlchk("let x = Customer[true].fetch('addr')", 			"SELECT a.cid,a.x,b.id as addr,b.y,b.cust FROM Customer as a LEFT JOIN Address as b ON a.cid=b.cust");
//		sqlchk("let x = Customer[true].fetch('addr').first()", 	"SELECT TOP 1 a.cid,a.x,b.id as addr,b.y,b.cust FROM Customer as a LEFT JOIN Address as b ON a.cid=b.cust");
//		sqlchk("let x = Customer[true].fetch('addr').orderBy('cid')", "SELECT a.cid,a.x,b.id as addr,b.y,b.cust FROM Customer as a LEFT JOIN Address as b ON a.cid=b.cust ORDER BY a.cid");
//		sqlchk("let x = Customer[true].x.fetch('addr')", 		"SELECT a.x FROM Customer as a");
//		sqlchk("let x = Customer[true].x.fks()", 				"SELECT a.x,b.id as addr FROM Customer as a LEFT JOIN Address as b ON a.cid=b.cust");
//		
//		sqlchkP("let x = Customer[addr < 111].fks()", 			"SELECT a.cid,a.x,b.id as addr FROM Customer as a LEFT JOIN Address as b ON a.cid=b.cust WHERE b.id < ?", "111");
	}


//	@Test
//	public void testDebugSQL() {
//		useCustomer11Src = true;
//
////		sqlchk("let x = Customer[true].fetch('addr').orderBy('cid')", "SELECT a.cid,a.x,b.id as addr,b.y,b.cust FROM Customer as a LEFT JOIN Address as b ON a.cid=b.cust ORDER BY a.cid");
////		sqlchk("let x = Customer[true].x.fetch('addr')", 		"SELECT a.x FROM Customer as a");
////		sqlchkP("let x = Customer[addr < 111].fks()", 			"SELECT a.cid,a.x,b.id as addr FROM Customer as a LEFT JOIN Address as b ON a.cid=b.cust WHERE b.id < ?", "111");
//		sqlchk("let x = Customer[true].x.fks()", 				"SELECT a.x,b.id as addr FROM Customer as a LEFT JOIN Address as b ON a.cid=b.cust");
//	}

	//---
	
	@Before
	public void init() {
		createDao();
	}

	protected void parseIntoLetSpans(String src, int expectedSize, String expected1) {
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		LetSpanEngine letEngine = new LetSpanEngine(delia.getFactoryService(), session.getExecutionContext().registry);
		List<LetSpan> spanL = letEngine.buildAllSpans(queryExp);
		
		JoinTreeEngine jtEngine = new JoinTreeEngine(delia.getFactoryService(), session.getExecutionContext().registry);
		List<JTElement> resultL = jtEngine.parse(queryExp, spanL);
		assertEquals(expectedSize, resultL.size());
		
		if (expected1 != null) {
			String s = resultL.get(0).toString();
			assertEquals(expected1, s);
		}
		
	}
	
}
