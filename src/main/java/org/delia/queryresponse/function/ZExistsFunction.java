package org.delia.queryresponse.function;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.queryresponse.ZQueryResponseFunctionBase;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class ZExistsFunction extends ZQueryResponseFunctionBase {
	public ZExistsFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx) {
		boolean b = !isEmpty(qresp);
		DValue dval = buildBoolVal(b);
		setSingletonResult(qresp, dval);
		return qresp; 
	}
	
	private boolean isEmpty(QueryResponse qresp) {
		List<DValue> dvalList = qresp.dvalList;
		
		if (CollectionUtils.isEmpty(dvalList)) {
			return true;
		}
		//TODO: we need to concern ourselves with null values??
		return false;
	}

}