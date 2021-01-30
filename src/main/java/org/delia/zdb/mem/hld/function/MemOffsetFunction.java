package org.delia.zdb.mem.hld.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.hld.QueryFnSpec;
import org.delia.queryfunction.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zdb.mem.hld.MemFunctionBase;

public class MemOffsetFunction extends MemFunctionBase {
	private int offsetAmount;
	public MemOffsetFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(HLSQuerySpan hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		List<DValue> dvalList = ctx.getDValList(); //use scope
		if (CollectionUtils.isEmpty(dvalList)) {
			return qresp; //nothing to sort
		}
		
		int offset = hlspan == null ? offsetAmount : hlspan.oloEl.offset; //getIntArg(qfe, ctx);
		ctx.currentOffset = offset;
		
		if (ctx.offsetLimitDirtyFlag) {
			doLimitAndOffset(ctx, qresp, dvalList);
		}
		return qresp;
	}
	
	protected void doLimitAndOffset(QueryFuncContext ctx, QueryResponse qresp, List<DValue> dvalList) {
		int offset = ctx.currentOffset;
		int pgSize = ctx.currentPgSize;
		
		ctx.currentOffset = 0; //reset
		
		if (ctx.offsetLimitDirtyFlag) {
			ctx.offsetLimitDirtyFlag = false;
			List<DValue> newlist = new ArrayList<>();
			int i = 0;
			for(DValue dval: dvalList) {
				if (offset > 0) {
					offset--;
					continue;
				}
				
				if (i == pgSize) {
					break;
				}
				newlist.add(dval);
				i++;
			}
			
			qresp.dvalList = newlist;
		}
	}
	@Override
	public QueryResponse process(QueryFnSpec hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		if (hlspan.filterFn.argL.isEmpty()) {
			DeliaExceptionHelper.throwError("queryfn-missing-arg", "offset function needs one int arg.");				
		}
		offsetAmount = hlspan.filterFn.argL.get(0).asInt();
		HLSQuerySpan jj = null;
		return process(jj, qresp, ctx);
	}

}