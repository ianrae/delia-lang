package org.delia.db.hls;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFieldExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.StringTrail;
import org.delia.zqueryresponse.LetSpan;
import org.delia.zqueryresponse.LetSpanEngine;
import org.delia.zqueryresponse.LetSpanRunner;
import org.delia.zqueryresponse.function.ZQueryResponseFunctionFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * HLS = High Level SQL
 * Yet another way to generate SQL.
 * A high-level representation of a Delia query
 * 
 * 
 * @author Ian Rae
 *
 */
public class HLSTests extends NewBDDBase {
	
	public interface HLSElement {
		
	}
	public static class MTElement implements HLSElement {
		public DStructType structType;

		public MTElement(DStructType fromType) {
			this.structType = fromType;
		}

		public String getTypeName() {
			return structType.getName();
		}

		@Override
		public String toString() {
			String s = String.format("MT:%s", structType.getName());
			return s;
		}
	}
	public static class FILElement implements HLSElement {
		public QueryExp queryExp;

		public FILElement(QueryExp queryExp) {
			this.queryExp = queryExp;
		}

		@Override
		public String toString() {
			String s = queryExp.toString();
			int pos = s.indexOf('[');
			s = pos < 0 ? s : s.substring(pos);
			pos = s.indexOf(']');
			s = pos < 0 ? s : s.substring(0, pos+1);
			
			return "" + s;
		}
		
	}
	public static class OLOElement implements HLSElement {
		public String orderBy; //may be null
		public Integer limit; //may be null
		public Integer offset; //may be null

		@Override
		public String toString() {
			String s = String.format("OLO:%s,%d,%d", orderBy, limit, offset);
			return s;
		}
	}
	public static class FElement implements HLSElement {
		public TypePair fieldPair;

		public FElement(TypePair fieldPair) {
			this.fieldPair = fieldPair;
		}

		public String getFieldName() {
			return fieldPair.name;
		}
		@Override
		public String toString() {
			String s = String.format("F:%s", fieldPair.name);
			return s;
		}
		
	}
	public static class RElement implements HLSElement {
		public TypePair rfieldPair;

		public RElement(TypePair rfieldPair) {
			this.rfieldPair = rfieldPair;
		}

		@Override
		public String toString() {
			String s = String.format("R:%s", rfieldPair.name);
			return s;
		}
		
	}
	public static class GElement implements HLSElement {
		public QueryFuncExp qfe;

		public GElement(QueryFuncExp qfe) {
			this.qfe = qfe;
		}

		@Override
		public String toString() {
			String s = String.format("%s", qfe.funcName);
			return s;
		}
		
	}
	public static class SUBElement implements HLSElement {
		public List<String> fetchL = new ArrayList<>();
		public List<String> fksL = new ArrayList<>();
		public boolean allFKs = false;
		
		public boolean containsFetch() {
			return !fetchL.isEmpty();
		}

		@Override
		public String toString() {
			StringJoiner joiner = new StringJoiner(",");
			for(String s: fetchL) {
				joiner.add(s);
			}
			for(String s: fksL) {
				joiner.add(s);
			}
			String s2 = String.format(",%s", joiner.toString());
			if (s2.equals(",")) {
				s2 = "";
			}
			String s = String.format("SUB:%b%s", allFKs, s2);
			return s;
		}

		public boolean isEmpty() {
			return fetchL.isEmpty() && fksL.isEmpty() && !allFKs;
		}
	}
	
	public static class HLSQuerySpan implements HLSElement {
		public DStructType fromType;
		public DType resultType;
		
		public MTElement mtEl;
		public FILElement filEl;
		public RElement rEl;
		public FElement fEl;
		public List<GElement> gElList = new ArrayList<>();
		public SUBElement subEl;
		public OLOElement oloEl;
		
		@Override
		public String toString() {
			StringJoiner joiner = new StringJoiner(",");
			String s4 = BuiltInTypes.convertDTypeNameToDeliaName(resultType.getName());
			String ss = String.format("%s->%s", fromType.getName(), s4);
			joiner.add(ss);
			
			if (mtEl != null) {
				joiner.add(mtEl.toString());
			}
			if (filEl != null) {
				joiner.add(filEl.toString());
			}
			if (rEl != null) {
				joiner.add(rEl.toString());
			}
			if (fEl != null) {
				joiner.add(fEl.toString());
			}
			StringJoiner subJ = new StringJoiner(",");
			for(GElement gel: gElList) {
				subJ.add(gel.toString());
			}
			String s3 = String.format("(%s)", subJ.toString());
			joiner.add(s3);
			
			if (subEl != null) {
				joiner.add(subEl.toString());
			}
			if (oloEl != null) {
				joiner.add(oloEl.toString());
			}
			
			String s = String.format("{%s}", joiner.toString());
			return s;
		}
	}

	public static class HLSQueryStatement implements HLSElement {
		public List<HLSQuerySpan> hlspanL = new ArrayList<>();
		public QueryExp queryExp;
		
		public HLSQuerySpan getMainHLSSpan() {
			return hlspanL.get(0);
		}

		@Override
		public String toString() {
			StringJoiner joiner = new StringJoiner(",");
			for(HLSQuerySpan hlspan: hlspanL) {
				joiner.add(hlspan.toString());
			}
			return joiner.toString();
		}
	}
	
	
	public static class HLSEngine extends ServiceBase {

		private QueryExp queryExp;
		private List<LetSpan> spanL;
		private DTypeRegistry registry;
		private DStructType mainStructType;
		private ZQueryResponseFunctionFactory fnFactory;

		public HLSEngine(FactoryService factorySvc, DTypeRegistry registry) {
			super(factorySvc);
			this.registry = registry;
			this.fnFactory = new ZQueryResponseFunctionFactory(factorySvc, null);
		}
		
		public HLSQueryStatement generateStatement(QueryExp queryExp, List<LetSpan> spanL) {
			this.queryExp = queryExp;
			this.spanL = spanL;
			this.mainStructType = (DStructType) registry.getType(queryExp.typeName);
			
			HLSQueryStatement hlstatement = new HLSQueryStatement();
			hlstatement.queryExp = queryExp;
			
			if (spanL.isEmpty()) {
				HLSQuerySpan hsltat = generateSpan(0, null);
				hlstatement.hlspanL.add(hsltat);
				return hlstatement;
			}
			
			//for some reason Customer[55].addr puts addr in span1.
			if (spanL.size() == 1) {
				LetSpan span1 = fixup(spanL.get(0));
				if (span1 != null) {
					spanL.add(0, span1); //insert as new first span
				}
			}
			
			int i = 0;
			for(LetSpan span: spanL) {
				HLSQuerySpan hsltat = generateSpan(i, span);
				hlstatement.hlspanL.add(hsltat);
				i++;
			}
			return hlstatement;
		}
		

		public LetSpan fixup(LetSpan span) {
			HLSQuerySpan hlstat = new HLSQuerySpan();
			hlstat.fromType = determineFromType(0);
			hlstat.mtEl = new MTElement(hlstat.fromType);
			hlstat.resultType = determineResultType(0);
			hlstat.filEl = null;
			
			TypePair rfieldPair = findLastRField(0);
			if (rfieldPair != null) {
				LetSpan span1 = new LetSpan(mainStructType);
				QueryFuncExp rqfe = findRFieldQFE(span, mainStructType);
//				span.qfeL.remove(rqfe);
//				span1.qfeL.add(rqfe);
				span1.qresp = span.qresp;
//				span.startsWithScopeChange = true;
				return span1;
			}
			return null;
		}		
		
		public HLSQuerySpan generateSpan(int i, LetSpan span) {
			HLSQuerySpan hlstat = new HLSQuerySpan();
			hlstat.fromType = determineFromType(i);
			hlstat.mtEl = new MTElement(hlstat.fromType);
			hlstat.resultType = determineResultType(i);
			hlstat.filEl = (i > 0) ? null : new FILElement(queryExp);
			
			if (spanL.isEmpty()) {
				return hlstat;
			}
			
			TypePair rfieldPair = findLastRField(i);
			if (rfieldPair != null) {
				hlstat.rEl = new RElement(rfieldPair);
			}
			
			TypePair fieldPair = findLastField(i);
			if (fieldPair != null) {
				hlstat.fEl = new FElement(fieldPair);
			}
			
			fillGElements(i, hlstat.gElList);

			hlstat.subEl = buildSubEl(i);
			hlstat.oloEl = buildOLO(i);
			
			//adjustments
			if (hlstat.fEl != null && hlstat.subEl != null) {
				if (hlstat.subEl.containsFetch()) {
					hlstat.subEl.fetchL.clear(); //fetch not needed
					if (hlstat.subEl.isEmpty()) {
						hlstat.subEl = null; //remove completely
					}
				}
			}
			
			return hlstat;
		}

		private OLOElement buildOLO(int iStart) {
			OLOElement oloel = new OLOElement();
			boolean found = false;
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
					i++;
					continue;
				} else if (i > iStart) {
					break;
				}
				
				for(QueryFuncExp qfe: span.qfeL) {
					if (qfe instanceof QueryFieldExp) {
					} else if (qfe.funcName.equals("orderBy")){
						String fieldName = qfe.argL.get(0).strValue();
						oloel.orderBy = fieldName;
						found = true;
					} else if (qfe.funcName.equals("limit")){
						IntegerExp exp = (IntegerExp) qfe.argL.get(0);
						oloel.limit = exp.val;
						found = true;
					} else if (qfe.funcName.equals("offset")){
						IntegerExp exp = (IntegerExp) qfe.argL.get(0);
						oloel.offset = exp.val;
						found = true;
					}
				}
				i++;
			}
			return found ? oloel : null;
		}

		private SUBElement buildSubEl(int iStart) {
			SUBElement subel = new SUBElement();
			boolean found = false;
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
					i++;
					continue;
				} else if (i > iStart) {
					break;
				}
				
				for(QueryFuncExp qfe: span.qfeL) {
					if (qfe instanceof QueryFieldExp) {
					} else if (qfe.funcName.equals("fetch")){
						String fieldName = qfe.argL.get(0).strValue();
						subel.fetchL.add(fieldName);
						found = true;
					} else if (qfe.funcName.equals("fks")){
						subel.allFKs = true;
						found = true;
					}
				}
				i++;
			}
			return found ? subel : null;
		}

		private void fillGElements(int iStart, List<GElement> gElList) {
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
					i++;
					continue;
				} else if (i > iStart) {
					break;
				}
				
				for(QueryFuncExp qfe: span.qfeL) {
					if (qfe instanceof QueryFieldExp) {
					} else if (! isOLOFn(qfe) && !isSUBFn(qfe)){
						GElement gel = new GElement(qfe);
						gElList.add(gel);
					}
				}
				i++;
			}
		}

		private boolean isOLOFn(QueryFuncExp qfe) {
			String fnName = qfe.funcName;
			String[] ar = {"orderBy", "limit", "offset"};
			List<String> oloList = Arrays.asList(ar);
			return oloList.contains(fnName);
		}
		private boolean isSUBFn(QueryFuncExp qfe) {
			String fnName = qfe.funcName;
			String[] ar = {"fetch", "fks"};
			List<String> oloList = Arrays.asList(ar);
			return oloList.contains(fnName);
		}

		private TypePair findLastField(int iStart) {
			DStructType currentType = mainStructType;
			TypePair lastField = null;
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
					i++;
					continue;
				} else if (i > iStart) {
					break;
				}
				
				TypePair fieldPair = findFField(span, currentType);
				if (fieldPair != null) {
					lastField = fieldPair;
				}
				i++;
			}
			return lastField;
		}
		private TypePair findLastRField(int iStart) {
			DStructType currentType = mainStructType;
			TypePair lastRField = null;
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
					i++;
					continue;
				} else if (i > iStart) {
					break;
				}
				
				TypePair rfieldPair = findRField(span, currentType);
				if (rfieldPair != null) {
					currentType = (DStructType) rfieldPair.type;
					lastRField = rfieldPair;
				}
				i++;
			}
			return lastRField;
		}

		private DStructType determineFromType(int iStart) {
			DStructType currentType = mainStructType;
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
					i++;
					continue;
				} else if (i > iStart) {
					break;
				}
				
				TypePair rfieldPair = findRField(span, currentType);
				if (rfieldPair != null) {
					currentType = (DStructType) rfieldPair.type;
				}
				i++;
			}
			
			return currentType;
		}

		private DType determineResultType(int iStart) {
			DStructType currentType = mainStructType;
			DType resultType = currentType;
			
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
					i++;
					continue;
				} else if (i > iStart) {
					break;
				}
				
				TypePair rfieldPair = findRField(span, currentType);
				if (rfieldPair != null) {
					currentType = (DStructType) rfieldPair.type;
					resultType = currentType;
				}
				
				TypePair fieldPair = findFField(span, currentType);
				if (fieldPair != null) {
					resultType = fieldPair.type;
				}
				
				DType gtype = findGType(span, currentType);
				if (gtype != null) {
					resultType = gtype;
				}
				i++;
			}
			
			return resultType;
		}

		private DType findGType(LetSpan span, DStructType currentType) {
			DType gtype = null;
			QueryFuncExp currentField = null;
			for(QueryFuncExp qfe: span.qfeL) {
				if (qfe instanceof QueryFieldExp) {
					currentField = qfe;
				} else {
					DType dtype = fnFactory.getResultType(qfe, currentType, currentField, registry);
					if (dtype != null) {
						gtype = dtype;
					}
				}
			}
			return gtype;
		}

		private TypePair findFField(LetSpan span, DStructType currentType) {
			for(QueryFuncExp qfe: span.qfeL) {
				if (qfe instanceof QueryFieldExp) {
					String fieldName = qfe.funcName;
					TypePair pair = DValueHelper.findField(currentType, fieldName);
					if (pair != null && !pair.type.isStructShape()) {
						return pair;
					}
				}
			}
			return null;
		}
		private TypePair findRField(LetSpan span, DStructType currentType) {
			for(QueryFuncExp qfe: span.qfeL) {
				if (qfe instanceof QueryFieldExp) {
					String fieldName = qfe.funcName;
					TypePair pair = DValueHelper.findField(currentType, fieldName);
					if (pair != null && pair.type.isStructShape()) {
						return pair;
					}
				}
			}
			return null;
		}
		private QueryFuncExp findRFieldQFE(LetSpan span, DStructType currentType) {
			for(QueryFuncExp qfe: span.qfeL) {
				if (qfe instanceof QueryFieldExp) {
					String fieldName = qfe.funcName;
					TypePair pair = DValueHelper.findField(currentType, fieldName);
					if (pair != null && pair.type.isStructShape()) {
						return qfe;
					}
				}
			}
			return null;
		}
		

		
		
	}
	
	
	@Test
	public void testOneSpanNoSub() {
		chk("let x = Flight[true]", "{Flight->Flight,MT:Flight,[true],()}");
		chk("let x = Flight[55]", "{Flight->Flight,MT:Flight,[55],()}");
		
		chk("let x = Flight[55].field1", "{Flight->int,MT:Flight,[55],F:field1,()}");
//		chk("let x = Flight[55].field1", "{Flight->Flight,MT:Flight,FIL:Flight[55],[]}");
		chk("let x = Flight[55].field1.min()", "{Flight->int,MT:Flight,[55],F:field1,(min)}");
		chk("let x = Flight[55].field1.orderBy('min')", "{Flight->int,MT:Flight,[55],F:field1,(),OLO:min,null,null}");
		chk("let x = Flight[55].field1.orderBy('min').offset(3)", "{Flight->int,MT:Flight,[55],F:field1,(),OLO:min,null,3}");
		chk("let x = Flight[55].field1.orderBy('min').offset(3).limit(5)", "{Flight->int,MT:Flight,[55],F:field1,(),OLO:min,5,3}");
		
		chk("let x = Flight[55].count()", "{Flight->long,MT:Flight,[55],(count)}");
		chk("let x = Flight[55].field1.count()", "{Flight->long,MT:Flight,[55],F:field1,(count)}");
		chk("let x = Flight[55].field1.distinct()", "{Flight->int,MT:Flight,[55],F:field1,(distinct)}");
		chk("let x = Flight[55].field1.exists()", "{Flight->boolean,MT:Flight,[55],F:field1,(exists)}");
		chk("let x = Flight[55].first()", "{Flight->Flight,MT:Flight,[55],(first)}");
	}
	
	@Test
	public void testOneSpanSub() {
		useCustomerSrc = true;
		chk("let x = Customer[true].fks()", "{Customer->Customer,MT:Customer,[true],(),SUB:true}");
		chk("let x = Customer[true].fetch('addr')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr}");
		
		chk("let x = Customer[true].fetch('addr').first()", "{Customer->Customer,MT:Customer,[true],(first),SUB:false,addr}");
		chk("let x = Customer[true].fetch('addr').orderBy('id')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr,OLO:id,null,null}");

		//this one doesn't need to do fetch since just getting x
		chk("let x = Customer[true].x.fetch('addr')", "{Customer->int,MT:Customer,[true],F:x,()}");
		
		chk("let x = Customer[true].x.fks()", "{Customer->int,MT:Customer,[true],F:x,(),SUB:true}");
	}
	
	@Test
	public void testOneRelation() {
		useCustomerSrc = true;
		chk("let x = Customer[true].addr", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,()}");
		
		chk("let x = Customer[true].fks()", "{Customer->Customer,MT:Customer,[true],(),SUB:true}");
		chk("let x = Customer[true].fetch('addr')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr}");
		
		chk("let x = Customer[true].fetch('addr').first()", "{Customer->Customer,MT:Customer,[true],(first),SUB:false,addr}");
		chk("let x = Customer[true].fetch('addr').orderBy('id')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr,OLO:id,null,null}");

		//this one doesn't need to do fetch since just getting x
		chk("let x = Customer[true].x.fetch('addr')", "{Customer->int,MT:Customer,[true],F:x,()}");
		
		chk("let x = Customer[true].x.fks()", "{Customer->int,MT:Customer,[true],F:x,(),SUB:true}");
		
		chk("let x = Customer[true].addr.fks()", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,(),SUB:true}");
		chk("let x = Customer[true].fks().addr", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,()}");
		chk("let x = Customer[true].fks().addr.fks()", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,(),SUB:true}");
		
		chk("let x = Customer[true].addr.orderBy('id')", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,(),OLO:id,null,null}");
		chk("let x = Customer[true].orderBy('id').addr", "{Customer->Customer,MT:Customer,[true],(),OLO:id,null,null},{Address->Address,MT:Address,R:addr,()}");
		chk("let x = Customer[true].orderBy('id').addr.orderBy('y')", "{Customer->Customer,MT:Customer,[true],(),OLO:id,null,null},{Address->Address,MT:Address,R:addr,(),OLO:y,null,null}");
	}
	
	
	@Test
	public void testDebug() {
//		chk("let x = Flight[55].first()", "{Flight->Flight,MT:Flight,[55],(first)}");
		
		useCustomerSrc = true;
//		chk("let x = Customer[true].fks()", "{Customer->Customer,MT:Customer,[true],(fks),SUB:true}");
//		chk("let x = Customer[true].x.fks()", "{Customer->int,MT:Customer,[true],F:x,(),SUB:true}");
		chk("let x = Customer[true].addr.fks()", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,(),SUB:true}");
//		chk("let x = Customer[true].fks().addr", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,()}");
//		chk("let x = Customer[true].fks().addr.fks()", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,(),SUB:true}");
		
		chk("let x = Customer[true].orderBy('id').addr.orderBy('y')", "{Customer->Customer,MT:Customer,[true],(),OLO:id,null,null},{Address->Address,MT:Address,R:addr,(),OLO:y,null,null}");
	}
	

	protected void chk(String src, String expected) {
		HLSQueryStatement hls = buildHLS(src);
		
		for(HLSQuerySpan hlspan: hls.hlspanL) {
			String hlstr = hlspan.toString();
			log.log(hlstr);
		}
//		assertEquals(1, hls.hlspanL.size());
		String hlstr = hls.toString();
		assertEquals(expected, hlstr);
	}

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


	private QueryExp compileQuery(String src) {
		String initialSrc = (useCustomerSrc) ? buildCustomerSrc() : buildSrc();
		DeliaDao dao = createDao(); 
		boolean b = dao.initialize(initialSrc);
		assertEquals(true, b);

		Delia delia = dao.getDelia();
		this.session = dao.getMostRecentSession();
		ResultValue res = delia.continueExecution(src, session);
		
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		LetStatementExp letStatement = findLet(sessimpl);
		
		QueryExp queryExp = (QueryExp) letStatement.value;
		QueryResponse qresp = (QueryResponse) res.val;
		
		MyLetSpanRunner myrunner = new MyLetSpanRunner();
		FetchRunner fetchRunner = null;
//		LetSpanEngine letEngine = new LetSpanEngine(delia.getFactoryService(), session.getExecutionContext().registry, fetchRunner, myrunner);
		
//		qresp = (QueryResponse) res.val;
//		qresp = letEngine.process(queryExp, qresp);
		return queryExp;
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
	protected Delia delia;
	protected DeliaSession session;
	protected boolean useCustomerSrc = false;
	
	@Before
	public void init() {
		createDao();
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		this.delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}

	private String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
	private String buildCustomerSrc() {
		String src = " type Customer struct {id int unique, x int, relation addr Address many optional  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  many optional } end";
		return src;
	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}

}
