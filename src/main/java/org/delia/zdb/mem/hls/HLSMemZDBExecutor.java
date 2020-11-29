package org.delia.zdb.mem.hls;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.delia.core.FactoryService;
import org.delia.db.QueryContext;
import org.delia.db.hls.GElement;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.queryresponse.FuncScope;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.queryresponse.function.ZCountFunction;
import org.delia.queryresponse.function.ZDistinctFunction;
import org.delia.queryresponse.function.ZExistsFunction;
import org.delia.queryresponse.function.ZFKsFunction;
import org.delia.queryresponse.function.ZFetchFunction;
import org.delia.queryresponse.function.ZFirstFunction;
import org.delia.queryresponse.function.ZLimitFunction;
import org.delia.queryresponse.function.ZMaxFunction;
import org.delia.queryresponse.function.ZMinFunction;
import org.delia.queryresponse.function.ZOffsetFunction;
import org.delia.queryresponse.function.ZOrderByFunction;
import org.delia.runner.QueryResponse;
import org.delia.zdb.mem.MemZDBExecutor;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.delia.zdb.mem.hls.function.MemCountFunction;
import org.delia.zdb.mem.hls.function.MemDistinctFunction;
import org.delia.zdb.mem.hls.function.MemExistsFunction;
import org.delia.zdb.mem.hls.function.MemFetchFunction;
import org.delia.zdb.mem.hls.function.MemFieldFunction;
import org.delia.zdb.mem.hls.function.MemFirstFunction;
import org.delia.zdb.mem.hls.function.MemFksFunction;
import org.delia.zdb.mem.hls.function.MemLimitFunction;
import org.delia.zdb.mem.hls.function.MemMaxFunction;
import org.delia.zdb.mem.hls.function.MemMinFunction;
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
		qtx.loadFKs = findAnyFKs(hls);
		QueryResponse qresp = doExecuteQuery(hls.querySpec, qtx); //do main filter
		
		//do all spans after first
		for(int i = 0; i < hls.hlspanL.size(); i++) {
			HLSQuerySpan hlspan = hls.hlspanL.get(i);
			
			List<MemFunction> actionL = buildActionsInOrder(hlspan);
			runActions(actionL, hlspan, qresp);
		}
		
		return qresp;
	}

	private boolean findAnyFKs(HLSQueryStatement hls) {
		//TODO this finds any fks. TODO later need to distinguish among multiple
		Optional<HLSQuerySpan> opt = hls.hlspanL.stream().filter(x -> (x.subEl != null && x.subEl.allFKs)).findAny();
		return opt.isPresent();
	}

	private List<MemFunction> buildActionsInOrder(HLSQuerySpan hlspan) {
		List<MemFunction> actionL = new ArrayList<>();
		//TODO handle immediate scope change. i think that's a struct field
		
		//then do order,limit,offset
		if (hlspan.oloEl != null) {
			addIf(actionL, (hlspan.oloEl.orderBy != null), new MemOrderByFunction(registry));
			addIf(actionL, (hlspan.oloEl.limit != null), new MemLimitFunction(registry));
			addIf(actionL, (hlspan.oloEl.offset != null), new MemOffsetFunction(registry));
		}
		
		if (hlspan.rEl != null) {
			actionL.add(new MemFieldFunction(registry, log, createFetchRunner()));
		} else if (hlspan.fEl != null) {
			actionL.add(new MemFieldFunction(registry, log, createFetchRunner()));
		}
				
		if (hlspan.subEl != null) {
			if (hlspan.subEl.allFKs) {
				actionL.add(new MemFksFunction(registry));
			} else if (!hlspan.subEl.fetchL.isEmpty()) {
				actionL.add(new MemFetchFunction(registry, createFetchRunner(), false));
			}			
		}
		
		for(GElement op: hlspan.gElList) {
			MemFunction fn = createGelMemFn(op);
			if (fn != null) {
				actionL.add(fn);
			}
		}
		
		return actionL;
	}

	private MemFunction createGelMemFn(GElement op) {
		switch(op.qfe.funcName) {
		case "min":
			return new MemMinFunction(factorySvc, registry, op);
		case "max":
			return new MemMaxFunction(factorySvc, registry, op);
		case "distinct":
			return new MemDistinctFunction(registry, op);
		case "count":
			return new MemCountFunction(registry, op);
		case "exists":
			return new MemExistsFunction(registry, op);
		case "first":
			return new MemFirstFunction(registry, op, true, false);
		case "last":
			return new MemFirstFunction(registry, op, false, false);
		case "ith":
			return new MemFirstFunction(registry, op, false, true);
		default:
			return null;
		}
	}

	private void addIf(List<MemFunction> actionL, boolean b, MemFunction fn) {
		if (b) {
			actionL.add(fn);
		}
	}

	private void runActions(List<MemFunction> actionL, HLSQuerySpan hlspan, QueryResponse qresp) {
		for(MemFunction fn: actionL) {
			qresp = runFn(hlspan, qresp, fn);
		}
	}

	private QueryResponse runFn(HLSQuerySpan hlspan, QueryResponse qresp, MemFunction fn) {
		QueryFuncContext ctx = new QueryFuncContext();
		ctx.scope = new FuncScope(qresp);
		ctx.offsetLimitDirtyFlag = hlspan.oloEl != null && hlspan.oloEl.limit != null;
		
		return fn.process(hlspan, qresp, ctx);
	}
}