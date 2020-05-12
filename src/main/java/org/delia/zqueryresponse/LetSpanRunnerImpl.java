package org.delia.zqueryresponse;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.QueryFieldExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.compiler.ast.StringExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.queryresponse.FuncScope;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.DRelation;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zqueryresponse.function.ZQueryResponseFunctionFactory;

public class LetSpanRunnerImpl extends ServiceBase implements LetSpanRunner {

	private DTypeRegistry registry;
	private FetchRunner fetchRunner;
	private ZQueryResponseFunctionFactory fnFactory;

	public LetSpanRunnerImpl(FactoryService factorySvc, DTypeRegistry registry, FetchRunner fetchRunner) {
		super(factorySvc);
		this.registry = registry;
		this.fetchRunner = fetchRunner;		
		this.fnFactory = new ZQueryResponseFunctionFactory(factorySvc, fetchRunner);
	}

	@Override
	public QueryResponse executeSpan(LetSpan span) {
		QueryResponse qresp = span.qresp;
		QueryFuncContext ctx = new QueryFuncContext();
		ctx.scope = new FuncScope(qresp);
		
		log.log("span:%d", span.qfeL.size());
		for(int i = 0; i < span.qfeL.size(); i++) {
			log.log("spantype:%s", span.dtype.getName());
			QueryFuncExp qfexp = span.qfeL.get(i);
//			ctx.pendingTrail.add(qfexp.funcName);
			
			if (qfexp instanceof QueryFieldExp) {
				qresp = processField(span, qfexp, qresp, ctx);
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

	private QueryResponse processField(LetSpan span, QueryFuncExp qff, QueryResponse qresp, QueryFuncContext ctx) {
		String fieldName = qff.funcName;
		log.log("qff: " + fieldName);
		
		if (CollectionUtils.isEmpty(qresp.dvalList)) {
			return qresp; //nothing to do
		}
		
		//span may start with a relation field (eg .addr)
		DValue firstRel = firstValueIsRelation(span, qff, qresp, ctx);
		if (firstRel != null) {
			qresp = doImplicitFetchIfNeeded(firstRel, qff, qresp, ctx);
			return qresp;
		}

		List<DValue> newList = new ArrayList<>();
		boolean checkFieldExists = true;
		for(DValue dval: qresp.dvalList) {
			if (dval == null) {
				newList.add(null);
			} else if (dval.getType().isStructShape()) {
				if (checkFieldExists) {
					checkFieldExists = false;
					DValueHelper.throwIfFieldNotExist("", fieldName, dval);
				}

				DValue inner = dval.asStruct().getField(qff.funcName);
				newList.add(inner);
			} else if (dval.getType().isRelationShape()) {
				DeliaExceptionHelper.throwError("let-unexpected-relation", "why this %s", fieldName);
			} else {
				//scalar
				newList.add(dval);
			}
		}
		qresp.dvalList = newList;
		return qresp;
	}

	private DValue firstValueIsRelation(LetSpan span, QueryFuncExp qff, QueryResponse qresp, QueryFuncContext ctx) {
		String fieldName = qff.funcName;
		if (!span.startsWithScopeChange) {
			return null;
		}
		span.startsWithScopeChange = false;

		DValue dval = qresp.dvalList.get(0);
		if (dval == null) {
			List<DValue> newList = new ArrayList<>();
			QueryResponse newRes = new QueryResponse();
			newRes.ok = true;
			newRes.dvalList = newList;
			ctx.scope.changeScope(newRes);  //new scope (empty)
			
			qresp.dvalList = newList;
			return null;
		}
		
		DValue inner = dval.asStruct().getField(fieldName); 
		return inner;
	}
	
	private QueryResponse doImplicitFetchIfNeeded(DValue firstRel, QueryFuncExp qff, QueryResponse qresp, QueryFuncContext ctx) {
		String fieldName = qff.funcName;
		
		if (firstRel != null && firstRel.getType().isRelationShape()) {
			QueryFuncExp qfe = new QueryFuncExp(99, new IdentExp("fetch"), null, true);
			qfe.argL.add(new StringExp(qff.funcName));
			qresp = executeFunc(qresp, qfe, fnFactory, ctx);
			
			List<DValue> newList = new ArrayList<>();
			for(DValue dval: qresp.dvalList) {
				DValue inner = dval.asStruct().getField(fieldName);
				if (inner == null) {
					continue;
				}
				DRelation drel = inner.asRelation();
				newList.addAll(drel.getFetchedItems());
			}
			QueryResponse newRes = new QueryResponse();
			newRes.ok = true;
			newRes.dvalList = newList;
			ctx.scope.changeScope(newRes);  //new scope
			
			qresp.dvalList = newList;
			return qresp;
		} else {
			return qresp;
		}
	}
}