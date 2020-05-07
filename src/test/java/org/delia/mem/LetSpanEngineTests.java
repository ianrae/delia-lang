package org.delia.mem;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
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
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.junit.Before;
import org.junit.Test;


public class LetSpanEngineTests extends NewBDDBase {
	
	public interface ZQueryResponseFunction {
		public QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx);
	}	
	public static abstract class ZQueryResponseFunctionBase implements ZQueryResponseFunction {

		protected DTypeRegistry registry;
		
		public ZQueryResponseFunctionBase(DTypeRegistry registry) {
			this.registry = registry;
		}
		public abstract QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx);
		
		protected String getStringArg(QueryFuncExp qfe, QueryFuncContext ctx) {
			String s = qfe.argL.get(0).strValue();
			return s;
		}
		protected int getIntArg(QueryFuncExp qfe, QueryFuncContext ctx) {
			Exp arg = qfe.argL.get(0);
			IntegerExp nexp = (IntegerExp) arg;
			return nexp.val;
		}
		
		protected void ensureFieldExists(QueryResponse qresp, String fnName, String fieldName) {
			if (CollectionUtils.isEmpty(qresp.dvalList)) {
				return;
			}
			DValue dval = qresp.dvalList.get(0);
			DValueHelper.throwIfFieldNotExist(fnName, fieldName, dval);
		}
		
	}	
	public static class ZOrderByFunction extends ZQueryResponseFunctionBase {
		public ZOrderByFunction(DTypeRegistry registry) {
			super(registry);
		}

		@Override
		public QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx) {
			List<DValue> dvalList = qresp.dvalList;
			if (dvalList == null || dvalList.size() <= 1) {
				return qresp; //nothing to sort
			}
			
			String fieldName = getStringArg(qfe, ctx); //"wid";
			
			TreeMap<Object,List<DValue>> map = new TreeMap<>();
			List<DValue> nulllist = new ArrayList<>();

			ensureFieldExists(qresp, "orderBy", fieldName);
			for(DValue dval: dvalList) {
				DValue inner = dval.asStruct().getField(fieldName);
				
				if (inner == null) {
					nulllist.add(dval);
				} else {
					List<DValue> valuelist = map.get(inner.getObject());
					if (valuelist == null) {
						valuelist = new ArrayList<>();
					}
					valuelist.add(inner);
					map.put(inner.getObject(), valuelist);
				}
			}
			
			List<DValue> newlist = new ArrayList<>();
			for(Object key: map.keySet()) {
				List<DValue> valuelist = map.get(key);
				newlist.addAll(valuelist);
			}
			
			//add null values
			boolean asc = isAsc(qfe, ctx);
			if (asc) {
				nulllist.addAll(newlist);
				newlist = nulllist;
			} else {
				newlist.addAll(nulllist);
			}
			
			if (! asc) {
				Collections.reverse(newlist);
			}
			
			qresp.dvalList = newlist;
			return qresp;
		}

		private boolean isAsc(QueryFuncExp qfe, QueryFuncContext ctx) {
			if (qfe.argL.size() == 2) {
				Exp arg = qfe.argL.get(1);
				return arg.strValue().equals("asc");
			}
			return true;
		}
	}	
	public static class OffsetFunction extends ZQueryResponseFunctionBase {
		public OffsetFunction(DTypeRegistry registry) {
			super(registry);
		}

		@Override
		public QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx) {
			
			List<DValue> dvalList = qresp.dvalList;
			if (dvalList == null || dvalList.size() <= 1) {
				return qresp; //nothing to sort
			}
			
			int offset = getIntArg(qfe, ctx);
			ctx.currentOffset = offset;
			
			doLimitAndOffset(ctx, qresp);
			return qresp;
		}
		
		protected boolean canExecuteInGivenOrder(QueryFuncContext ctx) {
			//Flight[true].offset(1).limit(2) is ok
			int pos1 = ctx.pendingTrail.getTrail().indexOf("offset");
			int pos2 = ctx.pendingTrail.getTrail().indexOf("limit");
			if (pos1 < 0 || pos2 < 0) {
				return true;
			} else if (pos1 < pos2) {
				return true;
			} else {
				return false;
			}
		}

		protected void doLimitAndOffset(QueryFuncContext ctx, QueryResponse qresp) {
			int offset = ctx.currentOffset;
			int pgSize = ctx.currentPgSize;
			
			ctx.currentOffset = 0; //reset
			
			if (ctx.offsetLimitDirtyFlag) {
				ctx.offsetLimitDirtyFlag = true;
				List<DValue> newlist = new ArrayList<>();
				int i = 0;
				List<DValue> dvalList = ctx.getDValList();
				for(DValue dval: dvalList) {
					if (offset > 0) {
						offset--;
						continue;
					}
					
					if (i == pgSize) {
						break;
					}
					newlist.add(dval);
					i++;
				}
				
				qresp.dvalList = newlist;
			}
		}
	}
	public static class ZQueryResponseFunctionFactory extends ServiceBase {
		private FetchRunner fetchRunner;

		public ZQueryResponseFunctionFactory(FactoryService factorySvc, FetchRunner fetchRunner) {
			super(factorySvc);
			this.fetchRunner = fetchRunner;
		}

		public ZQueryResponseFunction create(String fnName, DTypeRegistry registry) {
			switch(fnName) {
//			case "min":
//				return new MinFunction(registry);
//			case "max":
//				return new MaxFunction(registry);
//			case "count":
//				return new CountFunction(registry);
//			case "distinct":
//				return new DistinctFunction(registry);
//			case "exist":
//				return new ExistsFunction(registry);
//			case "fetch":
//				return new FetchFunction(registry, fetchRunner);
//			case "fks":
//				return new FKsFunction(registry, factorySvc.getConfigureService());
			case "orderBy":
				return new ZOrderByFunction(registry);
//			case "limit":
//				return new LimitFunction(registry);
//			case "offset":
//				return new OffsetFunction(registry);
//			case "first":
//				return new FirstFunction(registry, true, false);
//			case "last":
//				return new FirstFunction(registry, false, false);
//			case "ith":
//				return new FirstFunction(registry, false, true);
			default:
			{
				String msg = String.format("unknown fn: %s", fnName);
				et.add("unknown-query-function", msg);
				return null;
			}
			}
		}

		public boolean isPassFunction(int passNumber, String fnName) {
			String[] arPass1Fn = { "orderBy"};
			String[] arPass2Fn = { "offset" };
			String[] arPass3Fn = { "limit" };
			
			List<String> list = null;
			switch(passNumber) {
			case 1:
				list = Arrays.asList(arPass1Fn);
				break;
			case 2:
				list = Arrays.asList(arPass2Fn);
				break;
			case 3:
				list = Arrays.asList(arPass3Fn);
				break;
			default:
				break;
			}
			return list.contains(fnName);
		}
	}	
	
	
	public static class LetSpan {
		public DStructType structType;
		public List<QueryFuncExp> qfeL = new ArrayList<>();
		public QueryResponse qresp;
		
		public LetSpan(DType dtype) {
			this.structType = (DStructType) dtype;
		}
	}

	public static class LetSpanEngine extends ServiceBase {
		private DTypeRegistry registry;

		public LetSpanEngine(FactoryService factorySvc, DTypeRegistry registry) {
			super(factorySvc);
			this.registry = registry;
		}
		
		public QueryResponse process(QueryExp queryExp, QueryResponse qrespInitial) {
			List<LetSpan> spanL = buildSpans(queryExp);
			
			//execute span
			QueryResponse qresp = qrespInitial;
			for(LetSpan span: spanL) {
				span.qresp = qresp;
				span.qfeL = adjustExecutionOrder(span);
				qresp = executeSpan(span);
			}
			
			return qresp;
		}


		private QueryResponse executeSpan(LetSpan span) {
			FetchRunner fetchRunner = null;
			ZQueryResponseFunctionFactory fnFactory = new ZQueryResponseFunctionFactory(factorySvc, fetchRunner);
			
			QueryResponse qresp = span.qresp;
			
			for(int i = 0; i < span.qfeL.size(); i++) {
				QueryFuncExp qfexp = span.qfeL.get(i);
				if (qfexp instanceof QueryFieldExp) {
					qresp = processField(qfexp, qresp);
				} else {
					qresp = executeFunc(qresp, qfexp, fnFactory);
				}
			}
			return qresp;
		}
		
		private List<QueryFuncExp> adjustExecutionOrder(LetSpan span) {
			FetchRunner fetchRunner = null;
			ZQueryResponseFunctionFactory fnFactory = new ZQueryResponseFunctionFactory(factorySvc, fetchRunner);
			
			List<QueryFuncExp> newL = new ArrayList<>();
			
			//do orderby,offset,limit
			for(int passNumber = 1; passNumber <= 3; passNumber++) {
				List<QueryFuncExp> currentList = getPass(span, passNumber, fnFactory);
				newL.addAll(currentList);
			}
			
			//pass 4. fields and other fns
			for(int i = 0; i < span.qfeL.size(); i++) {
				QueryFuncExp qfexp = span.qfeL.get(i);
				if (newL.contains(qfexp)) {
					continue;
				}
				newL.add(qfexp);
			}
			return newL;
		}
		
		public QueryResponse processField(QueryFuncExp qff, QueryResponse qresp) {
			String fieldName = qff.funcName;
			log.log("qff: " + fieldName);
			
			if (qresp.dvalList == null || qresp.dvalList.size() <= 1) {
				return qresp; //nothing to do
			}

			List<DValue> newList = new ArrayList<>();
			boolean checkFieldExists = true;
			for(DValue dval: qresp.dvalList) {
				if (dval.getType().isStructShape()) {
					if (checkFieldExists) {
						checkFieldExists = false;
						DValueHelper.throwIfFieldNotExist("", fieldName, dval);
					}

					DValue inner = dval.asStruct().getField(qff.funcName);
					newList.add(inner);
				} else if (dval.getType().isRelationShape()) {
					DRelation drel = dval.asRelation();
					if (drel.getFetchedItems() == null) {
						DeliaExceptionHelper.throwError("cannot-access-field-without-fetch", "field '%s' cannot be accessed because fetch() was not called", qff.funcName);
					} else {
						newList.addAll(drel.getFetchedItems());
					}
				} else {
					//scalar
					newList.add(dval);
				}
			}
			qresp.dvalList = newList;
			return qresp;
		}

		private QueryResponse executeFunc(QueryResponse qresp, QueryFuncExp qfexp, ZQueryResponseFunctionFactory fnFactory) {
			String fnName = qfexp.funcName;
			log.log("qfn: " + fnName);
			ZQueryResponseFunction func = fnFactory.create(fnName, registry);
			if (func == null) {
				DeliaExceptionHelper.throwError("unknown-let-function", "Unknown let function '%s'", fnName);
			} else {
				QueryFuncContext ctx = new QueryFuncContext();
				qresp = func.process(qfexp, qresp, ctx);
			}
			return qresp;
		}

		private List<QueryFuncExp> getPass(LetSpan span, int passNum, ZQueryResponseFunctionFactory fnFactory) {
			List<QueryFuncExp> list = new ArrayList<>();
			for(QueryFuncExp qfe: span.qfeL) {
				if (fnFactory.isPassFunction(passNum, qfe.funcName)) {
					list.add(qfe);
				}
			}
			return list;
		}


		private List<LetSpan> buildSpans(QueryExp queryExp) {
			List<LetSpan> spanL = new ArrayList<>();
			LetSpan span = new LetSpan(registry.getType(queryExp.typeName));
			for(int i = 0; i < queryExp.qfelist.size(); i++) {
				QueryFuncExp qfexp = queryExp.qfelist.get(i);
				
				LetSpan possibleNewSpan = endsSpan(span, qfexp);
				if (possibleNewSpan != null) {
					spanL.add(span);
					span = possibleNewSpan;
				} else {
					span.qfeL.add(qfexp);
				}
			}
			
			if (! span.qfeL.isEmpty()) {
				spanL.add(span);
			}
			
			return spanL;
		}


		private LetSpan endsSpan(LetSpan span, QueryFuncExp qfexp) {
			if (qfexp instanceof QueryFieldExp) {
				QueryFieldExp qff = (QueryFieldExp) qfexp;
				String fieldName = qff.funcName;
				DType fieldType = DValueHelper.findFieldType(span.structType, fieldName);
				if (fieldType.isStructShape()) {
					LetSpan newSpan = new LetSpan(fieldType);
					return newSpan;
				}
			}
			return null;
		}
	}


	@Test
	public void testRaw() {
		String src = buildSrc();
		DeliaDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		Delia delia = dao.getDelia();
		src = "let x = Flight[true].orderBy('field1')";
		
		DeliaSession session = dao.getMostRecentSession();
		ResultValue res = delia.continueExecution(src, session);
		
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		LetStatementExp letStatement = findLet(sessimpl);
		
		LetSpanEngine letEngine = new LetSpanEngine(delia.getFactoryService(), session.getExecutionContext().registry);
		
		QueryExp queryExp = (QueryExp) letStatement.value;
		QueryResponse qresp = (QueryResponse) res.val;
		qresp = letEngine.process(queryExp, qresp);
		
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



	//---

	@Before
	public void init() {
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
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
