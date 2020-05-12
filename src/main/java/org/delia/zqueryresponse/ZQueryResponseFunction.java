package org.delia.zqueryresponse;

import org.delia.compiler.ast.QueryFuncExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;

public interface ZQueryResponseFunction {
	public QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx);
}