package org.delia.zdb.mem.hls;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.db.QueryContext;
import org.delia.db.hls.GElement;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.queryresponse.FuncScope;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.zdb.mem.MemZDBExecutor;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.delia.zdb.mem.hls.function.MemDistinctFunction;
import org.delia.zdb.mem.hls.function.MemFieldFunction;
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
			
			List<String> actionL = buildActionsInOrder(hlspan, hls.queryExp);
			runActions(actionL, hlspan, qresp);
			
		}
		
		return qresp;
	}

	private List<String> buildActionsInOrder(HLSQuerySpan hlspan, QueryExp queryExp) {
		List<String> actionL = new ArrayList<>();
		//TODO handle immediate scope change. i think that's a struct field
		//then do order,limit,offset
		addIf(actionL, (hlspan.oloEl.orderBy != null), "orderBy");
		addIf(actionL, (hlspan.oloEl.limit != null), "linit");
		addIf(actionL, (hlspan.oloEl.offset != null), "offset");
		
		//add rest in original order
		for(QueryFuncExp qfe: queryExp.qfelist) {
			if (!actionL.contains(qfe.funcName)) {
				actionL.add(qfe.funcName);
			}
		}
		
		return actionL;
	}

	private void addIf(List<String> actionL, boolean b, String action) {
		if (b) {
			actionL.add(action);
		}
	}

	private void runActions(List<String> actionL, HLSQuerySpan hlspan, QueryResponse qresp) {
		MemFunction fn = null;
		for(String action: actionL) {
			switch(action) {
			case "orderBy":
				fn = new MemOrderByFunction(registry);
				break;
			case "offset":
				fn = new MemOffsetFunction(registry);
				break;
			case "limit":
				fn = new MemLimitFunction(registry);
				break;
			case "distinct":
				fn = new MemDistinctFunction(registry);
				break;
			default:
				fn = new MemFieldFunction(registry, log, this.createFetchRunner());
				break;
			}
			
			qresp = runFn(hlspan, qresp, fn);
		}
	}

	private QueryResponse runFn(HLSQuerySpan hlspan, QueryResponse qresp, MemFunction fn) {
		QueryFuncContext ctx = new QueryFuncContext();
		ctx.scope = new FuncScope(qresp);
		ctx.offsetLimitDirtyFlag = hlspan.oloEl.limit != null;
		
		return fn.process(hlspan, qresp, ctx);
	}
}