package org.delia.queryresponse.function;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.QueryExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.queryresponse.QueryResponseFunctionBase;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class ExistsFunction extends QueryResponseFunctionBase {
	public ExistsFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(String fnName, QueryExp queryExp, QueryResponse qresp, QueryFuncContext ctx) {
		if (dbDoesThis(ctx)) {
			return qresp; //db already did it 
		}
		
		boolean b = !isEmpty(ctx);
		DValue dval = buildBoolVal(b);
		setSingletonResult(qresp, dval);
		return qresp; 
	}
	
	private boolean isEmpty(QueryFuncContext ctx) {
		List<DValue> dvalList = ctx.getDValList();
		
		if (CollectionUtils.isEmpty(dvalList)) {
			return true;
		}
		//TODO: we need to concern ourselves with null values??
		return false;
	}
}