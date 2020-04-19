package org.delia.queryresponse.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.queryresponse.QueryResponseFunctionBase;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;

public class OrderByFunction extends QueryResponseFunctionBase {
	public OrderByFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(String fnName, QueryExp queryExp, QueryResponse qresp, QueryFuncContext ctx) {
		if (ctx.dbCapabilities.supportsOrderBy()) {
			return qresp; //already done by db
		}
		
		List<DValue> dvalList = ctx.getDValList();
		if (dvalList == null || dvalList.size() <= 1) {
			return qresp; //nothing to sort
		}
		
		//Note. the actual dbinterface for a real db would do the sorting.
		//TODO: fix so we only sort for mem-db
		String fieldName = getStringArg(queryExp, ctx); //"wid";
		
		TreeMap<Object,DValue> map = new TreeMap<>();
		List<DValue> nulllist = new ArrayList<>();
		
		boolean checkFieldExists = true;
		for(DValue dval: dvalList) {
			if (checkFieldExists) {
				checkFieldExists = false;
				DValueHelper.throwIfFieldNotExist("orderBy", fieldName, dval);
			}
			
			DValue inner = dval.asStruct().getField(fieldName);
			if (inner == null) {
				nulllist.add(dval);
			} else {
				map.put(inner.getObject(), dval);
			}
		}
		
		List<DValue> newlist = new ArrayList<>();
		for(Object key: map.keySet()) {
			newlist.add(map.get(key));
		}
		
		//add null values
		boolean asc = isAsc(queryExp, ctx);
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

	private boolean isAsc(QueryExp queryExp, QueryFuncContext ctx) {
		QueryFuncExp exp = queryExp.qfelist.get(ctx.currentFnIndex);
		if (exp.argL.size() == 2) {
			Exp arg = exp.argL.get(1);
			return arg.strValue().equals("asc");
		}
		return true;
	}
	
}