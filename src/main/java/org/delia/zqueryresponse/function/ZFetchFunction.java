package org.delia.zqueryresponse.function;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.QueryFuncExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.DRelation;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;
import org.delia.zqueryresponse.ZQueryResponseFunctionBase;

public class ZFetchFunction extends ZQueryResponseFunctionBase {
	private FetchRunner fetchRunner;

	public ZFetchFunction(DTypeRegistry registry, FetchRunner fetchRunner) {
		super(registry);
		this.fetchRunner = fetchRunner;
	}

	@Override
	public QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx) {
//		QueryFuncExp qfe = ctx.qfexp;
		String targetFieldName = getStringArg(qfe, ctx); 

		//find type of targetFieldName. Address
		//query Address[addr-id] for each DValue in qresp.dvalList
		//TODO later use IN so can do single query
		
		QueryResponse qresResult = new QueryResponse();
		qresResult.ok = true;
		qresResult.dvalList = new ArrayList<>();
		List<DValue> dvalList = ctx.getDValList();
		List<DValue> newScopeList = new ArrayList<>();
		
		boolean checkFieldExists = true;
		for(DValue dval: dvalList) {
			if (checkFieldExists) {
				checkFieldExists = false;
				DValueHelper.throwIfFieldNotExist("fetch", targetFieldName, dval);
			}
			
			DValue inner = dval.asStruct().getField(targetFieldName);
			if (inner == null) {
				continue;
			}
			
			DRelation drel = inner.asRelation();
			QueryResponse qrespFetch = fetchRunner.load(drel);
			if (!qrespFetch.ok) {
				qresResult.ok = false;
				qresResult.err = qrespFetch.err;
			} else {
				qresResult.dvalList.addAll(qrespFetch.dvalList);
				newScopeList.addAll(qrespFetch.dvalList);
				
				//not sure if we need this bind flag
//				if (qresp.bindFetchFlag) {
					drel.setFetchedItems(qrespFetch.dvalList);
//				}
			}
		}
		
		QueryResponse newRes = new QueryResponse();
		newRes.ok = true;
		newRes.dvalList = newScopeList;
		ctx.scope.changeScope(newRes);
		
		//fetches changes the list. it's not just a hint to load sub-objects
		qresp.dvalList = newScopeList;
		return qresp;
	}

}