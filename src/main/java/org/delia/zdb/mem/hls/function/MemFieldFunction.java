package org.delia.zdb.mem.hls.function;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.newhls.QueryFnSpec;
import org.delia.db.newhls.RelationField;
import org.delia.db.newhls.StructFieldOpt;
import org.delia.log.Log;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.DRelation;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zdb.mem.hls.MemFunctionBase;

public class MemFieldFunction extends MemFunctionBase {
	private Log log;
	private FetchRunner fetchRunner;

	public MemFieldFunction(DTypeRegistry registry, Log log, FetchRunner fetchRunner) {
		super(registry);
		this.log = log;
		this.fetchRunner = fetchRunner;
	}

	@Override
	public QueryResponse process(HLSQuerySpan hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		if (CollectionUtils.isEmpty(qresp.dvalList)) {
			return qresp; //nothing to do
		}
		
		//span may start with a relation field (eg .addr)
		if (hlspan.rEl != null) {
			DValue firstRel = firstValueIsRelation(hlspan, qresp, ctx);
			if (firstRel != null) {
				qresp = doImplicitFetchIfNeeded(firstRel, hlspan, qresp, ctx);
				if (hlspan.fEl == null) {
					return qresp;
				}
			} else {
				return qresp;
			}
		}

		String fieldName = hlspan.fEl.getFieldName();
		log.logDebug("qff: " + fieldName);
		
		List<DValue> newList = new ArrayList<>();
		boolean checkFieldExists = true;
		for(DValue dval: qresp.dvalList) {
			if (dval == null) {
				newList.add(null);
			} else if (dval.getType().isStructShape()) {
				if (checkFieldExists) {
					checkFieldExists = false;
					DValueHelper.throwIfFieldNotExist("", fieldName, dval);
				}

				DValue inner = dval.asStruct().getField(fieldName);
				newList.add(inner);
			} else if (dval.getType().isRelationShape()) {
				DeliaExceptionHelper.throwError("let-unexpected-relation", "why this %s", fieldName);
			} else {
				//scalar
				newList.add(dval);
			}
		}
		qresp.dvalList = newList;
		return qresp;
	}

	private DValue firstValueIsRelation(HLSQuerySpan hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		if (hlspan.rEl == null) {
			return null;
		}
		
		String fieldName = hlspan.rEl.rfieldPair.name;
//		if (!span.startsWithScopeChange) {
//			return null;
//		}
//		span.startsWithScopeChange = false;

		//find any dval whose .field is non null
		DValue nonNullDVal = null;
		for(DValue dval: qresp.dvalList) {
			if (dval != null) {
				DValue inner = dval.asStruct().getField(fieldName); 
				if (inner != null) {
					nonNullDVal = dval;
					break;
				}
			}
		}
		
		if (nonNullDVal == null) {
			List<DValue> newList = new ArrayList<>();
			QueryResponse newRes = new QueryResponse();
			newRes.ok = true;
			newRes.dvalList = newList;
			ctx.scope.changeScope(newRes);  //new scope (empty)
			
			qresp.dvalList = newList;
			return null;
		}
		
		DValue inner = nonNullDVal.asStruct().getField(fieldName); 
		return inner;
	}
	
	private QueryResponse doImplicitFetchIfNeeded(DValue firstRel, HLSQuerySpan hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		String fieldName = hlspan.rEl.rfieldPair.name;
		
		if (firstRel != null && firstRel.getType().isRelationShape()) {
			MemFetchFunction fn = new MemFetchFunction(registry, fetchRunner, true);
			qresp = fn.process(hlspan, qresp, ctx);
			
//			QueryFuncExp qfe = new QueryFuncExp(99, new IdentExp("fetch"), null, true);
//			qfe.argL.add(new StringExp(qff.funcName));
//			qresp = executeFunc(qresp, qfe, fnFactory, ctx);
			
			List<DValue> newList = new ArrayList<>();
			for(DValue dval: qresp.dvalList) {
				DValue inner = dval.asStruct().getField(fieldName);
				if (inner == null) {
					continue;
				}
				DRelation drel = inner.asRelation();
				newList.addAll(drel.getFetchedItems());
			}
			QueryResponse newRes = new QueryResponse();
			newRes.ok = true;
			newRes.dvalList = newList;
			ctx.scope.changeScope(newRes);  //new scope
			
			qresp.dvalList = newList;
			return qresp;
		} else {
			return qresp;
		}
	}

	//------------------------------
	@Override
	public QueryResponse process(QueryFnSpec hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		if (CollectionUtils.isEmpty(qresp.dvalList)) {
			return qresp; //nothing to do
		}
		
		//span may start with a relation field (eg .addr)
		if (hlspan.structField.fieldType.isStructShape()) {
			DValue firstRel = xfirstValueIsRelation(hlspan.structField, qresp, ctx);
			if (firstRel != null) {
				//qresp = xdoImplicitFetchIfNeeded(firstRel, hlspan.structField, qresp, ctx);
//				if (hlspan.fEl == null) {
//					return qresp;
//				}
			} else {
				return qresp;
			}
		}

		String fieldName = hlspan.structField.fieldName; 
		log.logDebug("qff: " + fieldName);
		
		List<DValue> newList = new ArrayList<>();
		boolean checkFieldExists = true;
		for(DValue dval: qresp.dvalList) {
			if (dval == null) {
				newList.add(null);
			} else if (dval.getType().isStructShape()) {
				if (checkFieldExists) {
					checkFieldExists = false;
					DValueHelper.throwIfFieldNotExist("", fieldName, dval);
				}

				DValue inner = dval.asStruct().getField(fieldName);
				if (inner.getType().isRelationShape()) {
					DRelation drel = inner.asRelation();
					List<DValue> fetchedL = drel.getFetchedItems();
					if (fetchedL == null) {
						System.out.println("sdfsdfd");
					} else {
						newList.addAll(fetchedL);
					}
				} else {
					newList.add(inner);
				}
			} else if (dval.getType().isRelationShape()) {
				DeliaExceptionHelper.throwError("let-unexpected-relation", "why this %s", fieldName);
			} else {
				//scalar
				newList.add(dval);
			}
		}
		qresp.dvalList = newList;
		return qresp;
	}

	private DValue xfirstValueIsRelation(StructFieldOpt structField, QueryResponse qresp, QueryFuncContext ctx) {
//		if (hlspan.rEl == null) {
//			return null;
//		}
		
		String fieldName = structField.fieldName;

		//find any dval whose .field is non null
		DValue nonNullDVal = null;
		for(DValue dval: qresp.dvalList) {
			if (dval != null) {
				DValue inner = dval.asStruct().getField(fieldName); 
				if (inner != null) {
					nonNullDVal = dval;
					break;
				}
			}
		}
		
		if (nonNullDVal == null) {
			List<DValue> newList = new ArrayList<>();
			QueryResponse newRes = new QueryResponse();
			newRes.ok = true;
			newRes.dvalList = newList;
			ctx.scope.changeScope(newRes);  //new scope (empty)
			
			qresp.dvalList = newList;
			return null;
		}
		
		DValue inner = nonNullDVal.asStruct().getField(fieldName); 
		return inner;
	}
	
	private QueryResponse xdoImplicitFetchIfNeeded(DValue firstRel, StructFieldOpt structField, QueryResponse qresp, QueryFuncContext ctx) {
		String fieldName = structField.fieldName; //hlspan.rEl.rfieldPair.name;
		
		if (firstRel != null && firstRel.getType().isRelationShape()) {
			QueryFnSpec hlspan = new QueryFnSpec();
			hlspan.structField = structField;

			MemFetchFunction fn = new MemFetchFunction(registry, fetchRunner, true);
			qresp = fn.process(hlspan, qresp, ctx);
			
			List<DValue> newList = new ArrayList<>();
			for(DValue dval: qresp.dvalList) {
				DValue inner = dval.asStruct().getField(fieldName);
				if (inner == null) {
					continue;
				}
				DRelation drel = inner.asRelation();
				newList.addAll(drel.getFetchedItems());
			}
			QueryResponse newRes = new QueryResponse();
			newRes.ok = true;
			newRes.dvalList = newList;
			ctx.scope.changeScope(newRes);  //new scope
			
			qresp.dvalList = newList;
			return qresp;
		} else {
			return qresp;
		}
	}

}