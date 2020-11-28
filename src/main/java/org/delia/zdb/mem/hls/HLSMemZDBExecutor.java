package org.delia.zdb.mem.hls;

import org.delia.core.FactoryService;
import org.delia.db.QueryContext;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.queryresponse.FuncScope;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.zdb.mem.MemZDBExecutor;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.delia.zdb.mem.hls.function.MemLimitFunction;
import org.delia.zdb.mem.hls.function.MemOffsetFunction;
import org.delia.zdb.mem.hls.function.MemOrderByFunction;

/**
 * Use HLS data to do MEM query
 * @author ian
 *
 */
public class HLSMemZDBExecutor extends MemZDBExecutor {

	public HLSMemZDBExecutor(FactoryService factorySvc, MemZDBInterfaceFactory dbInterface) {
		super(factorySvc, dbInterface);
	}

	@Override
	public QueryResponse executeHLSQuery(HLSQueryStatement hls, String sql, QueryContext qtx) {
		log.log("ziggy!");
		qtx.pruneParentRelationFlag = false;
		qtx.loadFKs = false;
		QueryResponse qresp = doExecuteQuery(hls.querySpec, qtx);
		
		//do all spans after first
		for(int i = 0; i < hls.hlspanL.size(); i++) {
			HLSQuerySpan hlspan = hls.hlspanL.get(i);
			
			if (hlspan.oloEl != null) {
				if (hlspan.oloEl.orderBy != null) {
					MemOrderByFunction fn = new MemOrderByFunction(registry);
					runFn(hlspan, qresp, fn);
				}
				if (hlspan.oloEl.offset != null) {
					MemOffsetFunction fn = new MemOffsetFunction(registry);
					runFn(hlspan, qresp, fn);
				}
				if (hlspan.oloEl.limit != null) {
					MemLimitFunction fn = new MemLimitFunction(registry);
					runFn(hlspan, qresp, fn);
				}
			}
		}
		
		return qresp;
	}

	private void runFn(HLSQuerySpan hlspan, QueryResponse qresp, MemFunction fn) {
		QueryFuncContext ctx = new QueryFuncContext();
		ctx.scope = new FuncScope(qresp);
		ctx.offsetLimitDirtyFlag = hlspan.oloEl.limit != null;
		
		fn.process(hlspan, qresp, ctx);
	}
}