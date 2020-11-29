package org.delia.zdb.mem.hls.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.delia.db.hls.GElement;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

public class MemFirstFunction extends MemOffsetFunction {
	private boolean firstFlag;
	private boolean ithFlag;

	public MemFirstFunction(DTypeRegistry registry, boolean firstFlag, boolean ithFlag) {
		super(registry);
		this.firstFlag = firstFlag;
		this.ithFlag = ithFlag;
	}
	
	private GElement findGEl(HLSQuerySpan hlspan) {
		String target = ithFlag ? "ith" : (firstFlag ? "first" : "last");
		Optional<GElement> opt = hlspan.gElList.stream().filter(x -> x.qfe.funcName.equals(target)).findAny();
		return opt.isPresent() ? opt.get() : null;
	}

	@Override
	public QueryResponse process(HLSQuerySpan hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		List<DValue> dvalList = qresp.dvalList;
		if (dvalList == null || dvalList.size() <= 1) {
			if (ithFlag) {
				GElement gel = findGEl(hlspan);
				if (gel.qfe.argL.isEmpty()) {
					DeliaExceptionHelper.throwError("queryfn-bad-index", "bad index!! no index provided");				
				}
			}
			
			return qresp; //nothing to do
		}
		
		List<DValue> newlist = new ArrayList<>();
		int n;
		if (ithFlag) {
			GElement gel = findGEl(hlspan);
			n = getIntArg(gel.qfe, ctx); 
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
}