package org.delia.zdb.mem.hls.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.delia.db.hls.HLSQuerySpan;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.zdb.mem.hls.MemFunctionBase;

public class MemOrderByFunction extends MemFunctionBase {
	public MemOrderByFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(HLSQuerySpan hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		List<DValue> dvalList = ctx.getDValList(); //use scope
		if (dvalList == null || dvalList.size() <= 1) {
			return qresp; //nothing to sort
		}
		
		String fieldName = hlspan.oloEl.orderBy;// .finalWhereSql getStringArg(qfe, ctx); //"wid";
		
		TreeMap<Object,List<DValue>> map = new TreeMap<>();
		List<DValue> nulllist = new ArrayList<>();

		ensureFieldExists(dvalList, "orderBy", fieldName);
		for(DValue dval: dvalList) {
			DValue inner = dval.asStruct().getField(fieldName);
			
			if (inner == null) {
				nulllist.add(dval);
			} else {
				List<DValue> valuelist = map.get(inner.getObject());
				if (valuelist == null) {
					valuelist = new ArrayList<>();
				}
				valuelist.add(dval);
				map.put(inner.getObject(), valuelist);
			}
		}
		
		List<DValue> newlist = new ArrayList<>();
		for(Object key: map.keySet()) {
			List<DValue> valuelist = map.get(key);
			newlist.addAll(valuelist);
		}
		
		//add null values
		boolean asc = hlspan.oloEl.isAsc; //isAsc(qfe, ctx);
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

}