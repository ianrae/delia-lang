package org.delia.queryresponse.function;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.QueryExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.queryresponse.QueryResponseFunctionBase;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class CountFunction extends QueryResponseFunctionBase {
	public CountFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(String fnName, QueryExp queryExp, QueryResponse qresp, QueryFuncContext ctx) {
		if (dbDoesThis(ctx)) {
			return qresp; //db already did it 
		}
		
//		List<DValue> dvalList = ctx.getDValList();
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