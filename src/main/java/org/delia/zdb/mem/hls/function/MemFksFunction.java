package org.delia.zdb.mem.hls.function;

import org.delia.db.hls.HLSQuerySpan;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.mem.hls.MemFunctionBase;

public class MemFksFunction extends MemFunctionBase {
	public MemFksFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(HLSQuerySpan hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		//TODO: what to do here?
		return qresp;
	}
	

}