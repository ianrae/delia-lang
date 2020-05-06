package org.delia.queryresponse.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.compiler.ast.QueryExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.queryresponse.QueryResponseFunctionBase;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class DistinctFunction extends QueryResponseFunctionBase {

	public DistinctFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(String fnName, QueryExp queryExp, QueryResponse qresp, QueryFuncContext ctx) {
		if (dbDoesThis(ctx)) {
			return qresp; //db already did it 
		}
		
		List<DValue> dvalList = ctx.getDValList();
		if (dvalList == null || dvalList.size() <= 1) {
			return qresp; //nothing to sort
		}
		
		List<DValue> newlist = new ArrayList<>();
		
		//build list of distinct values, using map
		Map<String,DValue> map = new HashMap<>();
		for(DValue dval: dvalList) {
			String strval = dval.asString(); //use string for now
			if (! map.containsKey(strval)) {
				map.put(strval, dval);
				newlist.add(dval);
			}
		}
		
		qresp.dvalList = newlist;
		return qresp;
	}
	
}