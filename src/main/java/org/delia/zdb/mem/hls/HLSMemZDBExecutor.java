package org.delia.zdb.mem.hls;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFieldExp;
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
import org.delia.zdb.mem.hls.function.MemFksFunction;
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
		QueryResponse qresp = doExecuteQuery(hls.querySpec, qtx); //do main filter
		
		//do all spans after first
		for(int i = 0; i < hls.hlspanL.size(); i++) {
			HLSQuerySpan hlspan = hls.hlspanL.get(i);
			
			List<MemFunction> actionL = buildActionsInOrder(hlspan, hls.queryExp);
			runActions(actionL, hlspan, qresp);
		}
		
		return qresp;
	}

	private List<MemFunction> buildActionsInOrder(HLSQuerySpan hlspan, QueryExp queryExp) {
		List<MemFunction> actionL = new ArrayList<>();
		//TODO handle immediate scope change. i think that's a struct field
		
		//then do order,limit,offset
		if (hlspan.oloEl != null) {
			addIf(actionL, (hlspan.oloEl.orderBy != null), new MemOrderByFunction(registry));
			addIf(actionL, (hlspan.oloEl.limit != null), new MemLimitFunction(registry));
			addIf(actionL, (hlspan.oloEl.offset != null), new MemOffsetFunction(registry));
		}
		
		//add rest in original order
		String fieldToSkip = null;
		for(QueryFuncExp qfe: queryExp.qfelist) {
			if (qfe instanceof QueryFieldExp) {
				String fieldName = qfe.funcName;
				if (fieldToSkip != null) {
					fieldToSkip = null;
					continue;
				}
				
				if (hlspan.rEl != null) {
					if (hlspan.rEl.rfieldPair.name.equals(fieldName)) {
						actionL.add(new MemFieldFunction(registry, log, createFetchRunner()));
						if (hlspan.fEl != null && hlspan.fEl.fieldPair.equals(fieldName)) {
							fieldToSkip = fieldName;
						}
					}
				} else if (hlspan.fEl != null && hlspan.fEl.fieldPair.equals(fieldName)) {
					actionL.add(new MemFieldFunction(registry, log, createFetchRunner()));
				}
				
			} else if (!actionL.contains(qfe.funcName)) {
				String fnName = qfe.funcName;
				Optional<GElement> opt = hlspan.gElList.stream().filter(x -> x.qfe.funcName.equals(fnName)).findAny();
				if (! opt.isPresent()) {
					continue;
				}
				switch(qfe.funcName) {
				case "distinct":
					actionL.add(new MemDistinctFunction(registry));
					break;
				case "fks":
					actionL.add(new MemFksFunction(registry));
				default:
					break;
				}
			}
		}
		
		return actionL;
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