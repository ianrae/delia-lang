package org.delia.queryfunction.function;
//package org.delia.queryresponse.function;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.TreeMap;
//
//import org.delia.compiler.ast.Exp;
//import org.delia.compiler.ast.QueryFuncExp;
//import org.delia.queryresponse.QueryFuncContext;
//import org.delia.queryresponse.ZQueryResponseFunctionBase;
//import org.delia.runner.QueryResponse;
//import org.delia.type.DTypeRegistry;
//import org.delia.type.DValue;
//
//public class ZOrderByFunction extends ZQueryResponseFunctionBase {
//	public ZOrderByFunction(DTypeRegistry registry) {
//		super(registry);
//	}
//
//	@Override
//	public QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx) {
//		List<DValue> dvalList = ctx.getDValList(); //use scope
//		if (dvalList == null || dvalList.size() <= 1) {
//			return qresp; //nothing to sort
//		}
//		
//		String fieldName = getStringArg(qfe, ctx); //"wid";
//		
//		TreeMap<Object,List<DValue>> map = new TreeMap<>();
//		List<DValue> nulllist = new ArrayList<>();
//
//		ensureFieldExists(dvalList, "orderBy", fieldName);
//		for(DValue dval: dvalList) {
//			DValue inner = dval.asStruct().getField(fieldName);
//			
//			if (inner == null) {
//				nulllist.add(dval);
//			} else {
//				List<DValue> valuelist = map.get(inner.getObject());
//				if (valuelist == null) {
//					valuelist = new ArrayList<>();
//				}
//				valuelist.add(dval);
//				map.put(inner.getObject(), valuelist);
//			}
//		}
//		
//		List<DValue> newlist = new ArrayList<>();
//		for(Object key: map.keySet()) {
//			List<DValue> valuelist = map.get(key);
//			newlist.addAll(valuelist);
//		}
//		
//		//add null values
//		boolean asc = isAsc(qfe, ctx);
//		if (asc) {
//			nulllist.addAll(newlist);
//			newlist = nulllist;
//		} else {
//			newlist.addAll(nulllist);
//		}
//		
//		if (! asc) {
//			Collections.reverse(newlist);
//		}
//		
//		qresp.dvalList = newlist;
//		return qresp;
//	}
//
//	private boolean isAsc(QueryFuncExp qfe, QueryFuncContext ctx) {
//		if (qfe.argL.size() == 2) {
//			Exp arg = qfe.argL.get(1);
//			return arg.strValue().equals("asc");
//		}
//		return true;
//	}
//}