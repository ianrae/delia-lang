package org.delia.zqueryresponse.function;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.QueryFuncExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zqueryresponse.ZQueryResponseFunctionBase;

public class ZFirstFunction extends ZQueryResponseFunctionBase {
	private boolean firstFlag;
	private boolean ithFlag;

	public ZFirstFunction(DTypeRegistry registry, boolean firstFlag, boolean ithFlag) {
		super(registry);
		this.firstFlag = firstFlag;
		this.ithFlag = ithFlag;
	}

	@Override
	public QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx) {
//		//first and last done in db. ith will be done here
//		if (!ithFlag && dbDoesThis(ctx)) {
//			return qresp; //db already did it 
//		}
		
		List<DValue> dvalList = qresp.dvalList;
		if (dvalList == null || dvalList.size() <= 1) {
			return qresp; //nothing to do
		}
		
		List<DValue> newlist = new ArrayList<>();
		int n;
		if (ithFlag) {
			n = getIntArg(qfe, ctx); 
			if (n < 0 || n >= dvalList.size()) {
				DeliaExceptionHelper.throwError("queryfn-ith-bad-index", "bad index!! %d", n);				
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