package org.delia.queryresponse.function;

import java.util.List;

import org.delia.compiler.ast.QueryExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class LimitFunction extends OffsetFunction {
	public LimitFunction(DTypeRegistry registry) {
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
		
		//Note. the actual dbinterface for a real db would do limit
		//TODO: fix so we only sort for mem-db
		
		int limit = getIntArg(queryExp, ctx);
		ctx.currentPgSize = limit;
		
		if (canExecuteInGivenOrder(ctx)) {
			doLimitAndOffset(ctx, qresp);
		}
		return qresp;
	}
}