package org.delia.zdb.mem.hld.function;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.db.hls.GElement;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.hld.QueryFnSpec;
import org.delia.queryfunction.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class MemExistsFunction extends GelMemFunctionBase {

	public MemExistsFunction(DTypeRegistry registry, GElement op) {
		super(registry, op);
	}

	@Override
	public QueryResponse process(HLSQuerySpan hlspan, QueryResponse qresp, QueryFuncContext ctx) {
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
		return false;
	}
	@Override
	public QueryResponse process(QueryFnSpec hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		HLSQuerySpan hlspanx = null;
		return process(hlspanx, qresp, ctx);
	}

}