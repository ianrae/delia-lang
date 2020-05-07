package org.delia.zqueryresponse.function;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class ZLimitFunction extends ZOffsetFunction {
	public ZLimitFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx) {
		List<DValue> dvalList = qresp.dvalList;
		if (CollectionUtils.isEmpty(dvalList)) {
			return qresp; //nothing to sort
		}
		
		int limit = getIntArg(qfe, ctx);
		ctx.currentPgSize = limit;
		
		doLimitAndOffset(ctx, qresp);
		return qresp;
	}
}