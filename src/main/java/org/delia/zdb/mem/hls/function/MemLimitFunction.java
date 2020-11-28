package org.delia.zdb.mem.hls.function;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class MemLimitFunction extends MemOffsetFunction {
	public MemLimitFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(HLSQuerySpan hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		List<DValue> dvalList = qresp.dvalList;
		if (CollectionUtils.isEmpty(dvalList)) {
			return qresp; //nothing to sort
		}
		
		int limit = hlspan.oloEl.limit; //getIntArg(qfe, ctx);
		ctx.currentPgSize = limit;
		
		ctx.offsetLimitDirtyFlag = true;
		doLimitAndOffset(ctx, qresp, dvalList);
		return qresp;
	}
	

}