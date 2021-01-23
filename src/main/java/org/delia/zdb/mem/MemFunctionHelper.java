package org.delia.zdb.mem;

import java.util.ArrayList;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.hld.FetchSpec;
import org.delia.db.hld.FinalField;
import org.delia.db.hld.HLDQueryStatement;
import org.delia.db.hld.QScope;
import org.delia.db.hld.QueryFnSpec;
import org.delia.db.hld.RelationField;
import org.delia.db.hld.StructFieldOpt;
import org.delia.db.hld.cond.FilterFunc;
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
import org.delia.zdb.mem.hls.function.MemFetchFunction;
import org.delia.zdb.mem.hls.function.MemFieldFunction;
import org.delia.zdb.mem.hls.function.MemFirstFunction;
import org.delia.zdb.mem.hls.function.MemFksFunction;
import org.delia.zdb.mem.hls.function.MemLimitFunction;
import org.delia.zdb.mem.hls.function.MemMaxFunction;
import org.delia.zdb.mem.hls.function.MemMinFunction;
import org.delia.zdb.mem.hls.function.MemOffsetFunction;
import org.delia.zdb.mem.hls.function.MemOrderByFunction;

public class MemFunctionHelper extends ServiceBase {
	private DTypeRegistry registry;
	private FetchRunner fetchRunner;
	private MemZDBInterfaceFactory dbInterface;

	public MemFunctionHelper(FactoryService factorySvc, MemZDBInterfaceFactory dbInterface, DTypeRegistry registry, FetchRunner fetchRunner) {
		super(factorySvc);
		this.dbInterface = dbInterface;
		this.registry = registry;
		this.fetchRunner = fetchRunner;
	}

	public QueryResponse executeHLDQuery(HLDQueryStatement hld, QueryResponse qresp) {
		List<QScope> list = reorderIfNeeded(hld.hldquery.scopeL);

		for(QScope scope: list) {
			Object obj = scope.thing;
			if (obj == null) {
				continue; //can occur when child does .fks, which is a no-op
			}
			log.log("fn: %d: %s", scope.index, obj.toString());

			if (obj instanceof FinalField) {
				qresp = doField((FinalField) obj, qresp);
			} else if (obj instanceof QueryFnSpec) {
				qresp = doFunction((QueryFnSpec)obj, qresp);
			} else if (obj instanceof FetchSpec) {
				qresp = doFetch((FetchSpec)obj, qresp);
			} else if (obj instanceof RelationField) {
				qresp = doRelField((RelationField) obj, qresp);
			} else {
				DeliaExceptionHelper.throwNotImplementedError("unknown scope thing '%s'", obj == null ? "?" : obj.getClass().getSimpleName());
			}
		}

		return qresp;
	}
	
	/**
	 * We don't want to be too strict in the ordering of fns and fields.
	 * Customer[true].wid.orderBy('height')
	 * Since wid is scalar field, we'll move the orderBy before it.
	 * But need to be careful, and only reorder within a scope span
	 * (that is, up to the next field)
	 * 
	 * Also, offset, orderBy, and limit need to be done in correct order.
	 * 
	 * @param scopeL
	 * @return
	 */
	private List<QScope> reorderIfNeeded(List<QScope> scopeL) {
		List<QScope> list = new ArrayList<>();
		List<QScope> skipList = new ArrayList<>();

		int i = 0;
		for(QScope scope: scopeL) {
			Object obj = scope.thing;
			//log.log("X: %d: %s", scope.index, obj.getClass().getSimpleName());

			if (obj instanceof FinalField) {
				FinalField ff = (FinalField) obj;
				if (ff.isScalarField()) {
					int iEndSpan = findNextFieldOrEnd(scopeL, i+1);
					//re-order between i..k
					reOrderPagingFns(i+1, iEndSpan, list, skipList, scopeL, null);
				}
				list.add(scope);
			} else if (obj instanceof RelationField) {
				if (! skipList.contains(scope)) {
					list.add(scope);
				}
			} else if (! skipList.contains(scope)) {
				int iEndSpan = findNextFieldOrEnd(scopeL, i+1);

				reOrderPagingFns(i, iEndSpan, list, skipList, scopeL, scope);
				addIf(scope, list, skipList);
			}
			i++;
		}
		return list;
	}

	private void reOrderPagingFns(int i, int iEndSpan, List<QScope> list, List<QScope> skipList, List<QScope> scopeL, QScope oneWeAreAdding) {
		//must execute in this order: orderBy, offset, limit
		QScope fnscope = findFn("orderBy", i+1, iEndSpan, scopeL, oneWeAreAdding);
		addIf(fnscope, list, skipList);
		fnscope = findFn("offset", i+1, iEndSpan, scopeL, oneWeAreAdding);
		addIf(fnscope, list, skipList);
		fnscope = findFn("limit", i+1, iEndSpan, scopeL, oneWeAreAdding);
		addIf(fnscope, list, skipList);
	}

	private QScope findFn(String fnName, int iStart, int iEndSpan, List<QScope> scopeL, QScope oneWeAreAdding) {
		//re-order between i..k
		if (oneWeAreAdding != null && oneWeAreAdding.thing instanceof QueryFnSpec) {
			QueryFnSpec fnspec = (QueryFnSpec) oneWeAreAdding.thing;
			if (fnspec.isFn(fnName)) {
				return oneWeAreAdding;
			}
		}
		
		for(int k = iStart; k < iEndSpan; k++) {
			QScope sc2 = scopeL.get(k);
			
			if (sc2.thing instanceof QueryFnSpec) {
				QueryFnSpec fnspec = (QueryFnSpec) sc2.thing;
				if (fnspec.isFn(fnName)) {
					return sc2;
				}
			}
		}
		return null;
	}

	private void addIf(QScope scope, List<QScope> list, List<QScope> skipList) {
		if (scope != null && ! skipList.contains(scope)) {
			list.add(scope);
			skipList.add(scope);
		}
	}

	private int findNextFieldOrEnd(List<QScope> scopeL, int iStart) {
		for(int k = iStart; k < scopeL.size(); k++) {
			QScope scope = scopeL.get(k);
			if (scope.thing instanceof FinalField) {
				return k;
//				FinalField ff = (FinalField) scope.thing;
//				if (ff.isScalarField()) {
//					return k;
//				}
			}
		}
		return scopeL.size();
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

	private QueryResponse doField(FinalField ff, QueryResponse qresp) {
		MemFieldFunction fn = new MemFieldFunction(registry, log, fetchRunner);

		QueryFnSpec fnspec = createFnSpec("$FIELD");
		fnspec.structField = new StructFieldOpt(ff.structField.dtype, ff.structField.fieldName, ff.structField.fieldType);
		qresp = runFn(fnspec, qresp, fn, 0, false);
		return qresp;
	}
	private QueryResponse doRelField(RelationField rf, QueryResponse qresp) {
		MemFieldFunction fn = new MemFieldFunction(registry, log, fetchRunner);

		QueryFnSpec fnspec = createFnSpec("$FIELD");
		fnspec.structField = new StructFieldOpt(rf.dtype, rf.fieldName, rf.fieldType);
		qresp = runFn(fnspec, qresp, fn, 0, false);
		return qresp;
	}

	
	private QueryResponse doFetch(FetchSpec fetch, QueryResponse qresp) {
		if (fetch.isFK) {
			MemFksFunction fn = new MemFksFunction(registry, factorySvc, fetchRunner, dbInterface);
			QueryFnSpec fnspec = createFnSpec("$FKS");
			fnspec.structField = new StructFieldOpt(fetch.structType, fetch.fieldName, null);
			qresp = runFn(fnspec, qresp, fn, 0, false);
			return qresp;
		} else {
			MemFetchFunction fn = new MemFetchFunction(registry, fetchRunner, false);
			QueryFnSpec fnspec = createFnSpec("$FETCH");
			fnspec.structField = new StructFieldOpt(fetch.structType, fetch.fieldName, null);
			qresp = runFn(fnspec, qresp, fn, 0, false);
			return qresp;
		}
	}

	
	private QueryFnSpec createFnSpec(String fakeName) {
		QueryFnSpec fnspec = new QueryFnSpec();
		fnspec.filterFn = new FilterFunc();
		fnspec.filterFn.fnName = fakeName;
		return fnspec;
	}
 
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

	private QueryResponse runFn(QueryFnSpec fnspec, QueryResponse qresp, MemFunction fn, int i, boolean isFirstFn) {
		QueryFuncContext ctx = new QueryFuncContext();
		ctx.scope = new FuncScope(qresp);
		QueryResponse outputQresp = fn.process(fnspec, qresp, ctx);

		if (isFirstFn && (i == 0 || ctx.scope.hasChanged())) {
			//			pruneParentsAfterScopeChange(fnspec, qresp);
		}
		return outputQresp;
	}


}