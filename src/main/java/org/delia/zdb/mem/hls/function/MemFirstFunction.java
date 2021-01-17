package org.delia.zdb.mem.hls.function;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.hls.GElement;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.newhls.QueryFnSpec;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

public class MemFirstFunction extends GelMemFunctionBase {
	private boolean firstFlag;
	private boolean ithFlag;
	private Integer indexToRetrieve;

	public MemFirstFunction(DTypeRegistry registry, GElement op, boolean firstFlag, boolean ithFlag) {
		super(registry, op);
		this.firstFlag = firstFlag;
		this.ithFlag = ithFlag;
		if (op != null) {
			indexToRetrieve = getIntArg(op.qfe, null); 
		}
	}
	
	@Override
	public QueryResponse process(HLSQuerySpan hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		List<DValue> dvalList = qresp.dvalList;
		if (dvalList == null || dvalList.size() <= 1) {
			if (ithFlag) {
				GElement gel = op;
				if (gel.qfe.argL.isEmpty()) {
					DeliaExceptionHelper.throwError("queryfn-bad-index", "bad index!! no index provided");				
				}
			}
			
			return qresp; //nothing to do
		}
		
		List<DValue> newlist = new ArrayList<>();
		int n;
		if (ithFlag) {
			n = indexToRetrieve; 
			if (n < 0 || n >= dvalList.size()) {
				qresp.dvalList = newlist;
				return qresp;
//				DeliaExceptionHelper.throwError("queryfn-ith-bad-index", "bad index!! %d", n);				
			}
		} else {
			n = firstFlag ? 0 : dvalList.size() - 1;
		}
		DValue dval = dvalList.get(n);
		newlist.add(dval); //first one
		
		qresp.dvalList = newlist;
		return qresp;
	}
	
	@Override
	public QueryResponse process(QueryFnSpec hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		if (hlspan.filterFn.argL.isEmpty()) {
			DeliaExceptionHelper.throwError("queryfn-bad-index", "bad index!! no index provided");				
		}
		indexToRetrieve = hlspan.filterFn.argL.get(0).asInt();
		
		HLSQuerySpan jj = null;
		return process(jj, qresp, ctx);
	}
}