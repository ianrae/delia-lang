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

public class MemCountFunction extends GelMemFunctionBase {
	public MemCountFunction(DTypeRegistry registry, GElement op) {
		super(registry, op);
	}

	@Override
	public QueryResponse process(HLSQuerySpan hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		List<DValue> dvalList = qresp.dvalList;
		if (CollectionUtils.isEmpty(dvalList)) {
			DValue dval = buildLongVal(0);
			setSingletonResult(qresp, dval);
			return qresp; //count of empty set is 0
		}
		
		//don't count null values
		int n = 0;
		for(DValue dval: dvalList) {
			if (dval == null) {
				continue;
			}
			n++;
		}
		
		DValue dval = buildLongVal(n);
		setSingletonResult(qresp, dval);
		return qresp;
	}

	@Override
	public QueryResponse process(QueryFnSpec hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		HLSQuerySpan hlspanx = null;
		return process(hlspanx, qresp, ctx);
	}
	

}