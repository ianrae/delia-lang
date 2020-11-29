package org.delia.zdb.mem.hls.function;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.db.hls.GElement;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.queryresponse.QueryFuncContext;
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
}