package org.delia.queryresponse.function;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.QueryExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.queryresponse.QueryResponseFunctionBase;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class OffsetFunction extends QueryResponseFunctionBase {
	public OffsetFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(String fnName, QueryExp queryExp, QueryResponse qresp, QueryFuncContext ctx) {
		if (dbDoesThis(ctx)) {
			return qresp; //db already did it
		}
		
		List<DValue> dvalList = ctx.getDValList();
		if (dvalList == null || dvalList.size() <= 1) {
			return qresp; //nothing to sort
		}
		
		//Note. the actual dbinterface for a real db would do limit
		//TODO: fix so we only sort for mem-db
		
		int offset = getIntArg(queryExp, ctx);
		ctx.currentOffset = offset;
		
		doLimitAndOffset(ctx, qresp);
		return qresp;
	}
	
	protected boolean canExecuteInGivenOrder(QueryFuncContext ctx) {
		//Flight[true].offset(1).limit(2) is ok
		int pos1 = ctx.pendingTrail.getTrail().indexOf("offset");
		int pos2 = ctx.pendingTrail.getTrail().indexOf("limit");
		if (pos1 < 0 || pos2 < 0) {
			return true;
		} else if (pos1 < pos2) {
			return true;
		} else {
			return false;
		}
	}

	protected void doLimitAndOffset(QueryFuncContext ctx, QueryResponse qresp) {
		int offset = ctx.currentOffset;
		int pgSize = ctx.currentPgSize;
		
		ctx.currentOffset = 0; //reset
		
		if (ctx.offsetLimitDirtyFlag) {
			ctx.offsetLimitDirtyFlag = true;
			List<DValue> newlist = new ArrayList<>();
			int i = 0;
			List<DValue> dvalList = ctx.getDValList();
			for(DValue dval: dvalList) {
				if (offset > 0) {
					offset--;
					continue;
				}
				
				if (i == pgSize) {
					break;
				}
				newlist.add(dval);
				i++;
			}
			
			qresp.dvalList = newlist;
		}
	}
}