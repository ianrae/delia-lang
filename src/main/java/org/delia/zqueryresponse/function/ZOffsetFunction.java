package org.delia.zqueryresponse.function;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.zqueryresponse.ZQueryResponseFunctionBase;

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
			
			ctx.offsetLimitDirtyFlag = true;
			doLimitAndOffset(ctx, qresp);
			return qresp;
		}
		
//		protected boolean canExecuteInGivenOrder(QueryFuncContext ctx) {
//			//Flight[true].offset(1).limit(2) is ok
//			int pos1 = ctx.pendingTrail.getTrail().indexOf("offset");
//			int pos2 = ctx.pendingTrail.getTrail().indexOf("limit");
//			if (pos1 < 0 || pos2 < 0) {
//				return true;
//			} else if (pos1 < pos2) {
//				return true;
//			} else {
//				return false;
//			}
//		}

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