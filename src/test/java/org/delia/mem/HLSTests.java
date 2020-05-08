package org.delia.mem;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
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
import org.delia.zqueryresponse.LetSpanRunnerImpl;
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
		
	}
	public static class FILElement implements HLSElement {
		
	}
	public static class OLOElement implements HLSElement {
		
	}
	public static class FElement implements HLSElement {
		
	}
	public static class RElement implements HLSElement {
		
	}
	public static class GElement implements HLSElement {
		
	}
	public static class SUBElement implements HLSElement {
		
	}
	
	public static class HLSQueryStatement implements HLSElement {
		public MTElement mtEl;
		public FILElement filEl;
		public RElement rEl;
		public FElement fEl;
		public List<GElement> gElList = new ArrayList<>();
		public SUBElement subEl;
		public OLOElement oloEl;
		
		public DStructType fromType;
		public DType resultType;
		
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
		
		public HLSQueryStatement generate(QueryExp queryExp, List<LetSpan> spanL) {
			this.queryExp = queryExp;
			this.spanL = spanL;
			this.mainStructType = (DStructType) registry.getType(queryExp.typeName);

			
			HLSQueryStatement hlstat = new HLSQueryStatement();
			hlstat.fromType = determineFromType();
			hlstat.mtEl = new MTElement(hlstat.fromType);
			hlstat.resultType = determineResultType();
			
			if (spanL.isEmpty()) {
				return hlstat;
			}
			
			return hlstat;
		}

		private DStructType determineFromType() {
			DStructType currentType = mainStructType;
			for(LetSpan span: spanL) {
				TypePair rfieldPair = findRField(span, currentType);
				if (rfieldPair != null) {
					currentType = (DStructType) rfieldPair.type;
				}
			}
			
			return currentType;
		}

		private DType determineResultType() {
			DStructType currentType = mainStructType;
			DType resultType = currentType;
			
			for(LetSpan span: spanL) {
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
				
			}
			
			return resultType;
		}

		private DType findGType(LetSpan span, DStructType currentType) {
			DType gtype = null;
			for(QueryFuncExp qfe: span.qfeL) {
				if (qfe instanceof QueryFieldExp) {
				} else {
					String funcName = qfe.funcName;
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
		HLSQueryStatement hls = hlsEngine.generate(queryExp, spanL);
		assertEquals("Flight", hls.mtEl.getTypeName());
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
