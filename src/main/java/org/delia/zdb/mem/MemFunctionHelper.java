package org.delia.zdb.mem;

import java.util.ArrayList;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.newhls.HLDQueryStatement;
import org.delia.db.newhls.QueryFnSpec;
import org.delia.db.newhls.RelationField;
import org.delia.db.newhls.StructField;
import org.delia.db.newhls.StructFieldOpt;
import org.delia.queryresponse.FuncScope;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.mem.hls.MemFunction;
import org.delia.zdb.mem.hls.function.MemCountFunction;
import org.delia.zdb.mem.hls.function.MemDistinctFunction;
import org.delia.zdb.mem.hls.function.MemExistsFunction;
import org.delia.zdb.mem.hls.function.MemFieldFunction;
import org.delia.zdb.mem.hls.function.MemFirstFunction;
import org.delia.zdb.mem.hls.function.MemLimitFunction;
import org.delia.zdb.mem.hls.function.MemMaxFunction;
import org.delia.zdb.mem.hls.function.MemMinFunction;
import org.delia.zdb.mem.hls.function.MemOffsetFunction;
import org.delia.zdb.mem.hls.function.MemOrderByFunction;

public class MemFunctionHelper extends ServiceBase {
	private DTypeRegistry registry;
	private FetchRunner fetchRunner;

	public MemFunctionHelper(FactoryService factorySvc, MemZDBInterfaceFactory dbInterface, DTypeRegistry registry, FetchRunner fetchRunner) {
		super(factorySvc);
		this.registry = registry;
		this.fetchRunner = fetchRunner;
	}

	public QueryResponse executeHLDQuery(HLDQueryStatement hld, QueryResponse qresp) {
		//do all spans after first
		for(int i = 0; i < hld.hldquery.funcL.size(); i++) {
			QueryFnSpec hlspan = hld.hldquery.funcL.get(i);
			boolean beginsWithScopeChange = (i == 0 ) && !(hlspan.structField.dtype.getName().equals(hld.hldquery.fromType.getName()));

			List<MemFunction> actionL = buildActionsInOrder(hlspan, hld, beginsWithScopeChange);
			
			boolean isFirstFn = true;
			for(MemFunction fn: actionL) {
				qresp = runFn(hlspan, qresp, fn, i, isFirstFn);
				isFirstFn = false;
			}
		}
		
		if (hld.hldquery.finalField != null) {
			MemFunction fn = new MemFieldFunction(registry, log, fetchRunner);
			QueryFnSpec hlspan = new QueryFnSpec();
			StructField rf = hld.hldquery.finalField.structField;
			hlspan.structField = new StructFieldOpt(rf.dtype, rf.fieldName, rf.fieldType);
			qresp = runFn(hlspan, qresp, fn, 0, false);
		}
		
		return qresp;
	}
	private List<MemFunction> buildActionsInOrder(QueryFnSpec hlspan, HLDQueryStatement hls, boolean beginsWithScopeChange) {
		List<MemFunction> actionL = new ArrayList<>();
		if (beginsWithScopeChange) {
			actionL.add(new MemFieldFunction(registry, log, fetchRunner));
		}
		
		//then do orderBy,offset,limit
		int n = actionL.size();
		addIf(actionL, hlspan.isFn("orderBy"), new MemOrderByFunction(registry));
		addIf(actionL, hlspan.isFn("offset"), new MemOffsetFunction(registry));
		addIf(actionL, hlspan.isFn("limit"), new MemLimitFunction(registry));

		if (actionL.size() == n) {
			MemFunction fn = createMemFn(hlspan);
			if (fn != null) {
				actionL.add(fn);
			}
		}
		
		//TODO fix need the original order of Customer[1].fetch(sd).addr.limit(3)
//		if (! beginsWithScopeChange) {
//			if (hlspan..rEl != null) {
//				actionL.add(new MemFieldFunction(registry, log, createFetchRunner()));
//			} else if (hlspan.fEl != null) {
//				actionL.add(new MemFieldFunction(registry, log, createFetchRunner()));
//			}
//		}
				
//		if (hlspan.subEl != null) {
//			if (hlspan.subEl.allFKs) {
//				actionL.add(new MemFksFunction(registry));
//			} else if (!hlspan.subEl.fetchL.isEmpty()) {
//				actionL.add(new MemFetchFunction(registry, createFetchRunner(), false));
//			}			
//		}
		
//		for(GElement op: hlspan.gElList) {
//			MemFunction fn = createGelMemFn(op);
//			if (fn != null) {
//				actionL.add(fn);
//			}
//		}
		
		return actionL;
	}
	private MemFunction createMemFn(QueryFnSpec hlspan) {
		switch(hlspan.filterFn.fnName) {
		case "min":
			return new MemMinFunction(factorySvc, registry, null);
		case "max":
			return new MemMaxFunction(factorySvc, registry, null);
		case "distinct":
			return new MemDistinctFunction(registry, null);
		case "count":
			return new MemCountFunction(registry, null);
		case "exists":
			return new MemExistsFunction(registry, null);
		case "first":
			return new MemFirstFunction(registry, null, true, false);
		case "last":
			return new MemFirstFunction(registry, null, false, false);
		case "ith":
			return new MemFirstFunction(registry, null, false, true);
		default:
			return null;
		}
	}

	private void addIf(List<MemFunction> actionL, boolean b, MemFunction fn) {
		if (b) {
			actionL.add(fn);
		}
	}
	private QueryResponse runFn(QueryFnSpec hlspan, QueryResponse qresp, MemFunction fn, int i, boolean isFirstFn) {
		QueryFuncContext ctx = new QueryFuncContext();
		ctx.scope = new FuncScope(qresp);
//		ctx.offsetLimitDirtyFlag = hlspan.oloEl != null && hlspan.oloEl.limit != null;
		
		QueryResponse outputQresp = fn.process(hlspan, qresp, ctx);
		if (isFirstFn && (i == 0 || ctx.scope.hasChanged())) {
//			pruneParentsAfterScopeChange(hlspan, qresp);
		}
		return outputQresp;
	}


}