package org.delia.db.jointree;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFNameExp;
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
			DStructType structType = (DStructType) registry.getType(queryExp.typeName);
			for(LetSpan span: spanL) {
				if (!span.qfeL.isEmpty()) {
					QueryFuncExp qfe = span.qfeL.get(0);
					if (qfe.funcName.equals("fks")) {
						addFKs(span, resultL);
					} else if (qfe.funcName.equals("fetch")) {
						addFetch(span, qfe, resultL);
					} else {
						String fieldName = qfe.funcName;
						TypePair pair = DRuleHelper.findMatchingPair(structType, fieldName);
						if (pair != null) {
							addElement(structType, fieldName, (DStructType) pair.type, resultL);
						}
					}
				}
			}
			
			if (queryExp.filter.cond instanceof FilterOpFullExp) {
				FilterOpFullExp fofe = (FilterOpFullExp) queryExp.filter.cond;
				if (fofe.opexp1 instanceof FilterOpExp) {
					doFilterOpExp(fofe.opexp1, structType, resultL);
				}
				if (fofe.opexp2 instanceof FilterOpExp) {
					doFilterOpExp(fofe.opexp1, structType, resultL);
				}
			}
			
			return resultL;
		}
		
		
		private void doFilterOpExp(Exp opexp1, DStructType structType, List<JTElement> resultL) {
			FilterOpExp foe = (FilterOpExp) opexp1;
			doXNAFMultiExp(foe.op1, structType, resultL);
			doXNAFMultiExp(foe.op2, structType, resultL);
		}

		private void doXNAFMultiExp(Exp op, DStructType structType, List<JTElement> resultL) {
			if (! (op instanceof XNAFMultiExp)) {
				return;
			}
			XNAFMultiExp xx = (XNAFMultiExp) op;
			if (!xx.qfeL.isEmpty() && xx.qfeL.get(0) instanceof XNAFNameExp) {
				XNAFNameExp xne = (XNAFNameExp) xx.qfeL.get(0);
				
				String fieldName = xne.strValue();
				TypePair pair = DRuleHelper.findMatchingPair(structType, fieldName);
				if (pair != null) {
					addElement(structType, fieldName, (DStructType) pair.type, resultL);
				}
			}
		}

		private void addFetch(LetSpan span, QueryFuncExp qfe, List<JTElement> resultL) {
			DStructType structType = (DStructType) span.dtype;
			String fieldName = qfe.argL.get(0).strValue();
			TypePair pair = DRuleHelper.findMatchingPair(structType, fieldName);
			addElement(structType, fieldName, (DStructType) pair.type, resultL);
		}

		private void addFKs(LetSpan span, List<JTElement> resultL) {
			DStructType structType = (DStructType) span.dtype;
			for(TypePair pair: structType.getAllFields()) {
				if (pair.type.isStructShape()) {
					RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
					if (relinfo.isParent || RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
						addElement((DStructType) span.dtype, pair.name, (DStructType) pair.type, resultL);
					}
				}
			}
		}
		
		private void addElement(DStructType dtype, String field, DStructType fieldType, List<JTElement> resultL) {
			JTElement el = new JTElement();
			el.dtype = dtype;
			el.fieldName = field;
			el.fieldType = fieldType;
			
			String target = el.toString();
			Optional<JTElement> optExisting = resultL.stream().filter(x -> x.toString().equals(target)).findAny();
			if (optExisting.isPresent()) {
				return;
			}
			
			resultL.add(el);
		}
	}	
	
	
	@Test
	public void test() {
		useCustomer11Src = true;
		chkJoinTree("let x = Customer[55].fks()", "Customer|addr|Address"); 
		chkJoinTree("let x = Customer[55].x"); 
		
		chkJoinTree("let x = Customer[55].fetch('addr')", "Customer|addr|Address"); 
		chkJoinTree("let x = Customer[55].addr", "Customer|addr|Address"); 
		chkJoinTree("let x = Customer[55].addr.y", "Customer|addr|Address"); 
		//FUTURE later support order by doing implicit fetch. orderBy(addr.city)

		//FUTUER test double join   .addr.country
//		
		chkJoinTree("let x = Customer[addr < 111]", "Customer|addr|Address"); 
	}

	@Test
	public void testDouble() {
		useCustomer11Src = true;
		chkJoinTree("let x = Customer[addr < 111].fetch('addr')", "Customer|addr|Address"); 
	}

	@Test
	public void testDebugSQL() {
		useCustomer11Src = true;
		chkJoinTree("let x = Customer[addr < 111]", "Customer|addr|Address"); 

	}

	//---
	
	@Before
	public void init() {
		createDao();
	}

	protected void chkJoinTree(String src, String ...arExpected) {
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		LetSpanEngine letEngine = new LetSpanEngine(delia.getFactoryService(), session.getExecutionContext().registry);
		List<LetSpan> spanL = letEngine.buildAllSpans(queryExp);
		
		JoinTreeEngine jtEngine = new JoinTreeEngine(delia.getFactoryService(), session.getExecutionContext().registry);
		List<JTElement> resultL = jtEngine.parse(queryExp, spanL);
		int n = arExpected.length;
		assertEquals(n, resultL.size());
		
		for(String expected: arExpected) {
			String s = resultL.get(0).toString();
			assertEquals(expected, s);
		}
		
	}
	
}
