package org.delia.queryresponse.function;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.queryresponse.ZQueryResponseFunctionBase;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class ZOffsetFunction extends ZQueryResponseFunctionBase {
		public ZOffsetFunction(DTypeRegistry registry) {
			super(registry);
		}

		@Override
		public QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx) {
			List<DValue> dvalList = ctx.getDValList(); //use scope
			if (CollectionUtils.isEmpty(dvalList)) {
				return qresp; //nothing to sort
			}
			
			int offset = getIntArg(qfe, ctx);
			ctx.currentOffset = offset;
			
			if (ctx.offsetLimitDirtyFlag) {
				doLimitAndOffset(ctx, qresp, dvalList);
			}
			return qresp;
		}
		
		protected void doLimitAndOffset(QueryFuncContext ctx, QueryResponse qresp, List<DValue> dvalList) {
			int offset = ctx.currentOffset;
			int pgSize = ctx.currentPgSize;
			
			ctx.currentOffset = 0; //reset
			
			if (ctx.offsetLimitDirtyFlag) {
				ctx.offsetLimitDirtyFlag = false;
				List<DValue> newlist = new ArrayList<>();
				int i = 0;
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