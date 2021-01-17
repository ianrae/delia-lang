package org.delia.zdb.mem;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.newhls.HLDQueryStatement;
import org.delia.db.newhls.QScope;
import org.delia.db.newhls.QueryFnSpec;
import org.delia.db.newhls.RelationField;
import org.delia.db.newhls.StructFieldOpt;
import org.delia.queryresponse.FuncScope;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;
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
		for(QScope scope: hld.hldquery.scopeL) {
			if (scope.thing instanceof RelationField) {
				qresp = doField((RelationField) scope.thing, qresp);
			} else if (scope.thing instanceof QueryFnSpec) {
				qresp = doFunction((QueryFnSpec)scope.thing, qresp);
			}
		}
		
//		
//		//do all spans after first
//		for(int i = 0; i < hld.hldquery.funcL.size(); i++) {
//			QueryFnSpec fnspec = hld.hldquery.funcL.get(i);
//			boolean beginsWithScopeChange = (i == 0 ) && !(fnspec.structField.dtype.getName().equals(hld.hldquery.fromType.getName()));
//
//			List<MemFunction> actionL = buildActionsInOrder(fnspec, hld, beginsWithScopeChange);
//			
//			boolean isFirstFn = true;
//			for(MemFunction fn: actionL) {
//				qresp = runFn(fnspec, qresp, fn, i, isFirstFn);
//				isFirstFn = false;
//			}
//		}
//		
//		if (hld.hldquery.finalField != null) {
//			MemFunction fn = new MemFieldFunction(registry, log, fetchRunner);
//			QueryFnSpec fnspec = new QueryFnSpec();
//			StructField rf = hld.hldquery.finalField.structField;
//			fnspec.structField = new StructFieldOpt(rf.dtype, rf.fieldName, rf.fieldType);
//			qresp = runFn(fnspec, qresp, fn, 0, false);
//		}
		
		return qresp;
	}
	private QueryResponse doFunction(QueryFnSpec fnspec, QueryResponse qresp) {
		MemFunction fn;
		if (fnspec.isFn("orderBy")) {
			fn = new MemOrderByFunction(registry);
		} else if (fnspec.isFn("offset")) {
			fn =  new MemOffsetFunction(registry);
		} else if (fnspec.isFn("limit")) {
			fn =  new MemLimitFunction(registry);
		} else {
			fn = createMemFn(fnspec);
		}

		if (fn == null) {
			DeliaExceptionHelper.throwNotImplementedError("unknown MEM fn '%s'", fnspec.filterFn.fnName);
			return null;
		} else {
			qresp = runFn(fnspec, qresp, fn, 0, false);
			return qresp;
		}
	}

	private QueryResponse doField(RelationField rf, QueryResponse qresp) {
		MemFieldFunction fn = new MemFieldFunction(registry, log, fetchRunner);
		
		QueryFnSpec fnspec = new QueryFnSpec();
		fnspec.filterFn.fnName = "$FIELD";
		fnspec.structField = new StructFieldOpt(rf.dtype, rf.fieldName, rf.fieldType);
		qresp = runFn(fnspec, qresp, fn, 0, false);
		return qresp;
	}

//	private List<MemFunction> buildActionsInOrder(QueryFnSpec fnspec, HLDQueryStatement hls, boolean beginsWithScopeChange) {
//		List<MemFunction> actionL = new ArrayList<>();
//		if (beginsWithScopeChange) {
//			actionL.add(new MemFieldFunction(registry, log, fetchRunner));
//		}
//		
//		//then do orderBy,offset,limit
//		int n = actionL.size();
//		addIf(actionL, fnspec.isFn("orderBy"), new MemOrderByFunction(registry));
//		addIf(actionL, fnspec.isFn("offset"), new MemOffsetFunction(registry));
//		addIf(actionL, fnspec.isFn("limit"), new MemLimitFunction(registry));
//
//		if (actionL.size() == n) {
//			MemFunction fn = createMemFn(fnspec);
//			if (fn != null) {
//				actionL.add(fn);
//			}
//		}
//		
//		//TODO fix need the original order of Customer[1].fetch(sd).addr.limit(3)
////		if (! beginsWithScopeChange) {
////			if (fnspec..rEl != null) {
////				actionL.add(new MemFieldFunction(registry, log, createFetchRunner()));
////			} else if (fnspec.fEl != null) {
////				actionL.add(new MemFieldFunction(registry, log, createFetchRunner()));
////			}
////		}
//				
////		if (fnspec.subEl != null) {
////			if (fnspec.subEl.allFKs) {
////				actionL.add(new MemFksFunction(registry));
////			} else if (!fnspec.subEl.fetchL.isEmpty()) {
////				actionL.add(new MemFetchFunction(registry, createFetchRunner(), false));
////			}			
////		}
//		
////		for(GElement op: fnspec.gElList) {
////			MemFunction fn = createGelMemFn(op);
////			if (fn != null) {
////				actionL.add(fn);
////			}
////		}
//		
//		return actionL;
//	}
	private MemFunction createMemFn(QueryFnSpec fnspec) {
		switch(fnspec.filterFn.fnName) {
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

//	private void addIf(List<MemFunction> actionL, boolean b, MemFunction fn) {
//		if (b) {
//			actionL.add(fn);
//		}
//	}
	private QueryResponse runFn(QueryFnSpec fnspec, QueryResponse qresp, MemFunction fn, int i, boolean isFirstFn) {
		QueryFuncContext ctx = new QueryFuncContext();
		ctx.scope = new FuncScope(qresp);
//		ctx.offsetLimitDirtyFlag = fnspec.oloEl != null && fnspec.oloEl.limit != null;
		
		QueryResponse outputQresp = fn.process(fnspec, qresp, ctx);
		if (isFirstFn && (i == 0 || ctx.scope.hasChanged())) {
//			pruneParentsAfterScopeChange(fnspec, qresp);
		}
		return outputQresp;
	}


}