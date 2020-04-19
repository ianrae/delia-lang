package org.delia.queryresponse;

import org.delia.compiler.ast.QueryExp;
import org.delia.runner.QueryResponse;

public interface QueryResponseFunction {
	public QueryResponse process(String fnName, QueryExp queryExp, QueryResponse qresp, QueryFuncContext ctx);
}