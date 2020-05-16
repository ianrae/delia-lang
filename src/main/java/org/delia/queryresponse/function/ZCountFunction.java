package org.delia.queryresponse.function;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.queryresponse.ZQueryResponseFunctionBase;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class ZCountFunction extends ZQueryResponseFunctionBase {
	public ZCountFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx) {
		List<DValue> dvalList = qresp.dvalList;
		if (CollectionUtils.isEmpty(dvalList)) {
			DValue dval = buildLongVal(0);
			setSingletonResult(qresp, dval);
			return qresp; //count of empty set is 0
		}
		
		//don't count null values
		int n = 0;
		for(DValue dval: dvalList) {
			if (dval == null) {
				continue;
			}
			n++;
		}
		
		DValue dval = buildLongVal(n);
		setSingletonResult(qresp, dval);
		return qresp;
	}
}