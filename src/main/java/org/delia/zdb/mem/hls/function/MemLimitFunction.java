package org.delia.zdb.mem.hls.function;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.newhls.QueryFnSpec;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

public class MemLimitFunction extends MemOffsetFunction {
	private int limitAmount;

	public MemLimitFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(HLSQuerySpan hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		List<DValue> dvalList = qresp.dvalList;
		if (CollectionUtils.isEmpty(dvalList)) {
			return qresp; //nothing to sort
		}
		
		int limit = (hlspan == null) ? limitAmount : hlspan.oloEl.limit; //getIntArg(qfe, ctx);
		ctx.currentPgSize = limit;
		
		ctx.offsetLimitDirtyFlag = true;
		doLimitAndOffset(ctx, qresp, dvalList);
		return qresp;
	}
	
	@Override
	public QueryResponse process(QueryFnSpec hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		if (hlspan.filterFn.argL.isEmpty()) {
			DeliaExceptionHelper.throwError("queryfn-missing-arg", "limit function needs one int arg.");				
		}
		limitAmount = hlspan.filterFn.argL.get(0).asInt();
		
		HLSQuerySpan jj = null;
		return process(jj, qresp, ctx);
	}

}