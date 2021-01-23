package org.delia.zdb.mem.hls;

import org.delia.db.hls.HLSQuerySpan;
import org.delia.hld.QueryFnSpec;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;

public interface MemFunction {
	public QueryResponse process(HLSQuerySpan hlspan, QueryResponse qresp, QueryFuncContext ctx);
	public QueryResponse process(QueryFnSpec hlspan, QueryResponse qresp, QueryFuncContext ctx);
}