//package org.delia.queryresponse;
//
//import java.util.ArrayList;
//
//import org.delia.compiler.ast.Exp;
//import org.delia.compiler.ast.IntegerExp;
//import org.delia.compiler.ast.QueryExp;
//import org.delia.compiler.ast.QueryFuncExp;
//import org.delia.runner.QueryResponse;
//import org.delia.type.DTypeRegistry;
//import org.delia.type.DValue;
//import org.delia.valuebuilder.ScalarValueBuilder;
//
//public abstract class QueryResponseFunctionBase implements QueryResponseFunction {
//	protected DTypeRegistry registry;
//
//	public QueryResponseFunctionBase(DTypeRegistry registry) {
//		this.registry = registry;
//	}
//
//	@Override
//	public abstract QueryResponse process(String fnName, QueryExp queryExp, QueryResponse qresp, QueryFuncContext ctx);
//
//	protected String getStringArg(QueryExp queryExp, QueryFuncContext ctx) {
//		QueryFuncExp qfe = queryExp.qfelist.get(ctx.currentFnIndex);
//		String s = qfe.argL.get(0).strValue();
//		return s;
//	}
//	protected int getIntArg(QueryExp queryExp, QueryFuncContext ctx) {
//		QueryFuncExp qfe = queryExp.qfelist.get(ctx.currentFnIndex);
//		Exp arg = qfe.argL.get(0);
//		IntegerExp nexp = (IntegerExp) arg;
//		return nexp.val;
//	}
//	
//	
//	protected void setSingletonResult(QueryResponse qresp, DValue dval) {
//		qresp.dvalList = new ArrayList<>();
//		qresp.dvalList.add(dval);
//		//TODO: should we create a new qresp obj??
//	}
//
//	protected DValue buildIntVal(int max) {
//		ScalarValueBuilder builder = new ScalarValueBuilder(null, registry);
//		DValue dval = builder.buildInt(max);
//		return dval;
//	}
//	protected DValue buildLongVal(long max) {
//		ScalarValueBuilder builder = new ScalarValueBuilder(null, registry);
//		DValue dval = builder.buildLong(max);
//		return dval;
//	}
//	protected DValue buildNumberVal(double max) {
//		ScalarValueBuilder builder = new ScalarValueBuilder(null, registry);
//		DValue dval = builder.buildNumber(max);
//		return dval;
//	}
//	protected DValue buildBoolVal(boolean b) {
//		ScalarValueBuilder builder = new ScalarValueBuilder(null, registry);
//		DValue dval = builder.buildBoolean(b);
//		return dval;
//	}
//	protected DValue buildStringVal(String s) {
//		ScalarValueBuilder builder = new ScalarValueBuilder(null, registry);
//		DValue dval = builder.buildString(s);
//		return dval;
//	}
//	
//	protected boolean dbDoesThis(QueryFuncContext ctx) {
//		if (ctx.dbCapabilities.supportsOffsetAndLimit()) {
//			return true; //db already did it (we assume any db supporting offset also supports count
//		}
//		return false;
//	}
//}