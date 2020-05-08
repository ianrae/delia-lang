package org.delia.mem;


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

		public Object getTypeName() {
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
			return "FIL:" + queryExp.strValue();
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
			String s = String.format("G:%s", qfe.funcName);
			return s;
		}
		
	}
	public static class SUBElement implements HLSElement {
		public List<String> fetchL = new ArrayList<>();
		public List<String> fksL = new ArrayList<>();
		public boolean allFKs = false;

		@Override
		public String toString() {
			StringJoiner joiner = new StringJoiner(",");
			for(String s: fetchL) {
				joiner.add(s);
			}
			for(String s: fksL) {
				joiner.add(s);
			}
			String s = String.format("SUB:%b,%s", allFKs, joiner.toString());
			return s;
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
			joiner.add("{");
			String ss = String.format("%s->%s", fromType.getName(), resultType.getName());
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
			joiner.add("[");
			StringJoiner subJ = new StringJoiner(",");
			for(GElement gel: gElList) {
				subJ.add(gel.toString());
			}
			joiner.add(subJ.toString());
			joiner.add("]");
			
			if (subEl != null) {
				joiner.add(subEl.toString());
			}
			if (oloEl != null) {
				joiner.add(oloEl.toString());
			}
			joiner.add("}");
			return joiner.toString();
		}
	}

	public static class HLSQueryStatement implements HLSElement {
		public List<HLSQuerySpan> hlspanL = new ArrayList<>();
		
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
			if (spanL.isEmpty()) {
				HLSQuerySpan hsltat = generateSpan(0, null);
				hlstatement.hlspanL.add(hsltat);
				return hlstatement;
			}
			
			int i = 0;
			for(LetSpan span: spanL) {
				HLSQuerySpan hsltat = generateSpan(i, span);
				hlstatement.hlspanL.add(hsltat);
				i++;
			}
			return hlstatement;
		}
		
		
		public HLSQuerySpan generateSpan(int i, LetSpan span) {
			HLSQuerySpan hlstat = new HLSQuerySpan();
			hlstat.fromType = determineFromType(i);
			hlstat.mtEl = new MTElement(hlstat.fromType);
			hlstat.resultType = determineResultType(i);
			
			if (spanL.isEmpty()) {
				return hlstat;
			}
			
			hlstat.filEl = (i > 0) ? null : new FILElement(queryExp);
			
			TypePair rfieldPair = findLastRField(i);
			if (rfieldPair != null) {
				hlstat.rEl = new RElement(rfieldPair);
			}
			
			TypePair fieldPair = findLastField(i);
			if (rfieldPair != null) {
				hlstat.fEl = new FElement(fieldPair);
			}
			
			fillGElements(i, hlstat.gElList);

			hlstat.subEl = buildSubEl(i);
			hlstat.oloEl = buildOLO(i);
			
			return hlstat;
		}

		private OLOElement buildOLO(int iStart) {
			OLOElement oloel = new OLOElement();
			boolean found = false;
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
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
					continue;
				} else if (i > iStart) {
					break;
				}
				
				for(QueryFuncExp qfe: span.qfeL) {
					if (qfe instanceof QueryFieldExp) {
					} else if (! isOLOFn(qfe)){
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

		private TypePair findLastField(int iStart) {
			DStructType currentType = mainStructType;
			TypePair lastField = null;
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
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
			for(QueryFuncExp qfe: span.qfeL) {
				if (qfe instanceof QueryFieldExp) {
				} else {
					DType dtype = fnFactory.getResultType(qfe, currentType, registry);
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
					if (!pair.type.isStructShape()) {
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
					if (pair.type.isStructShape()) {
						return pair;
					}
				}
			}
			return null;
		}
		

		
		
	}
	@Test
	public void test1() {
		QueryExp queryExp = compileQuery("let x = Flight[true]");
		LetSpanEngine letEngine = new LetSpanEngine(delia.getFactoryService(), session.getExecutionContext().registry, null, null);
		List<LetSpan> spanL = letEngine.buildAllSpans(queryExp);
		
		
		HLSEngine hlsEngine = new HLSEngine(delia.getFactoryService(), session.getExecutionContext().registry);
		HLSQueryStatement hls = hlsEngine.generateStatement(queryExp, spanL);
		assertEquals(1, hls.hlspanL.size());
		HLSQuerySpan hlspan = hls.hlspanL.get(0);
		assertEquals("Flight", hlspan.mtEl.getTypeName());
	}
	
	

	private QueryExp compileQuery(String src) {
		String initialSrc = buildSrc();
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
	private Delia delia;
	private DeliaSession session;
	
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

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}

}
