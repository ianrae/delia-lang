package org.delia.queryresponse.function;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFieldExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBCapabilties;
import org.delia.queryresponse.FuncScope;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.queryresponse.QueryResponseFunction;
import org.delia.queryresponse.QueryResponseFunctionFactory;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;
import org.delia.util.StringTrail;

public class QueryFuncOrFieldRunner extends ServiceBase {
	private QueryResponseFunctionFactory funcFactory;
	private DTypeRegistry registry;
	private DBCapabilties dbCapabilities;
	
	public QueryFuncOrFieldRunner(FactoryService factorySvc, DTypeRegistry registry, FetchRunner fetchRunner, DBCapabilties dbCapabilties) {
		super(factorySvc);
		this.registry = registry;
		this.funcFactory = new QueryResponseFunctionFactory(factorySvc, fetchRunner);
		this.dbCapabilities = dbCapabilties;
	}

	public QueryResponse process(QueryExp queryExp, QueryResponse qresp0) {
		QueryResponse result = qresp0;
		if (CollectionUtils.isNotEmpty(queryExp.qfelist)) {
			QueryFuncContext ctx = new QueryFuncContext();
			ctx.scope = new FuncScope(result);
			ctx.dbCapabilities = this.dbCapabilities;
			buildPendingTrail(ctx, queryExp);
			
			for(QueryFuncExp qfexp: queryExp.qfelist) {
				ctx.qfexp = qfexp;
				
				if (qfexp instanceof QueryFieldExp) {
					//only do this for mem db.
					if (endsWithFn(queryExp) && ctx.dbCapabilities.supportsOffsetAndLimit()) {
						continue;
					}
						
					QueryFieldExp qff = (QueryFieldExp) qfexp;
					String fieldName = qff.funcName;
//					log.log("qff: " + fieldName);
					List<DValue> newList = new ArrayList<>();

					boolean checkFieldExists = true;
					for(DValue dval: ctx.getDValList()) {
						if (checkFieldExists) {
							checkFieldExists = false;
							DValueHelper.throwIfFieldNotExist("", fieldName, dval);
						}
						DValue inner = dval.asStruct().getField(qff.funcName);
						newList.add(inner);
					}
					result.dvalList = newList;
				} else { //it's a fn to run
					String fnName = qfexp.funcName;
//					log.log("qfn: " + fnName);
					
					QueryResponseFunction func = funcFactory.create(fnName, registry);
					if (func == null) {
						result.err = et.getLastError();
						result.ok = false;
					} else {
						result = func.process(fnName, queryExp, result, ctx);
					}
				}
				ctx.currentFnIndex++;
			}
		}
		
		return result;
	}

	//let x = Customer[55].fetch('addr').id -do here
	//let x = Customer[55].wid.min() -ends in fn. done in db
	private boolean endsWithFn(QueryExp queryExp) {
		boolean lastWasFn = false;
		for(QueryFuncExp qfexp: queryExp.qfelist) {
			if (qfexp instanceof QueryFieldExp) {
				lastWasFn = false;
			} else {
				lastWasFn = true;
			}
		}
		return lastWasFn;
	}

	public void buildPendingTrail(QueryFuncContext ctx, QueryExp queryExp) {
		StringTrail trail = new StringTrail();
		
		for(QueryFuncExp qfexp: queryExp.qfelist) {
			if (qfexp instanceof QueryFieldExp) {
				QueryFieldExp qff = (QueryFieldExp) qfexp;
				String fieldName = qff.funcName;
//				log.log("qff: " + fieldName);
				break; //we're done
			} else { //it's a fn to run
				String fnName = qfexp.funcName;
//				log.log("qfn: " + fnName);
				trail.add(fnName);
			}
		}
		
		ctx.pendingTrail = trail;
	}
}