package org.delia.zqueryresponse;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.QueryFieldExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.queryresponse.FuncScope;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.DRelation;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zqueryresponse.function.ZQueryResponseFunctionFactory;

public class LetSpanRunnerImpl extends ServiceBase implements LetSpanRunner {

	private DTypeRegistry registry;
	private FetchRunner fetchRunner;

	public LetSpanRunnerImpl(FactoryService factorySvc, DTypeRegistry registry, FetchRunner fetchRunner) {
		super(factorySvc);
		this.registry = registry;
		this.fetchRunner = fetchRunner;
	}

	@Override
	public QueryResponse executeSpan(LetSpan span) {
		ZQueryResponseFunctionFactory fnFactory = new ZQueryResponseFunctionFactory(factorySvc, fetchRunner);
		
		QueryResponse qresp = span.qresp;
		QueryFuncContext ctx = new QueryFuncContext();
		ctx.scope = new FuncScope(qresp);

		for(int i = 0; i < span.qfeL.size(); i++) {
			QueryFuncExp qfexp = span.qfeL.get(i);
			if (qfexp instanceof QueryFieldExp) {
				qresp = processField(qfexp, qresp, ctx);
			} else {
				qresp = executeFunc(qresp, qfexp, fnFactory, ctx);
			}
		}
		return qresp;
	}
	private QueryResponse executeFunc(QueryResponse qresp, QueryFuncExp qfexp, ZQueryResponseFunctionFactory fnFactory, QueryFuncContext ctx) {
		String fnName = qfexp.funcName;
		log.log("qfn: " + fnName);
		ZQueryResponseFunction func = fnFactory.create(fnName, registry);
		if (func == null) {
			DeliaExceptionHelper.throwError("unknown-let-function", "Unknown let function '%s'", fnName);
		} else {
			qresp = func.process(qfexp, qresp, ctx);
		}
		return qresp;
	}

	private QueryResponse processField(QueryFuncExp qff, QueryResponse qresp, QueryFuncContext ctx) {
		String fieldName = qff.funcName;
		log.log("qff: " + fieldName);
		
		if (CollectionUtils.isEmpty(qresp.dvalList)) {
			return qresp; //nothing to do
		}

		List<DValue> newList = new ArrayList<>();
		boolean checkFieldExists = true;
		for(DValue dval: qresp.dvalList) {
			if (dval.getType().isStructShape()) {
				if (checkFieldExists) {
					checkFieldExists = false;
					DValueHelper.throwIfFieldNotExist("", fieldName, dval);
				}

				DValue inner = dval.asStruct().getField(qff.funcName);
				newList.add(inner);
			} else if (dval.getType().isRelationShape()) {
				DRelation drel = dval.asRelation();
				if (drel.getFetchedItems() == null) {
					DeliaExceptionHelper.throwError("cannot-access-field-without-fetch", "field '%s' cannot be accessed because fetch() was not called", qff.funcName);
				} else {
					newList.addAll(drel.getFetchedItems());
					QueryResponse newRes = new QueryResponse();
					newRes.ok = true;
					newRes.dvalList = newList;
					ctx.scope.changeScope(newRes);  //new scope
				}
			} else {
				//scalar
				newList.add(dval);
			}
		}
		qresp.dvalList = newList;
		return qresp;
	}

}