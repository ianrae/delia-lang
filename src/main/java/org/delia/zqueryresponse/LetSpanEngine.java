package org.delia.zqueryresponse;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFieldExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.util.DValueHelper;
import org.delia.zqueryresponse.function.ZQueryResponseFunctionFactory;

public class LetSpanEngine extends ServiceBase {
	private DTypeRegistry registry;
	private LetSpanRunner runner;
	private ZQueryResponseFunctionFactory fnFactory;

	public LetSpanEngine(FactoryService factorySvc, DTypeRegistry registry, LetSpanRunner runner) {
		super(factorySvc);
		this.registry = registry;
		this.runner = runner;
		
		FetchRunner fetchRunner = null;  //TODO: fix

		this.fnFactory = new ZQueryResponseFunctionFactory(factorySvc, fetchRunner);
	}
	
	public QueryResponse process(QueryExp queryExp, QueryResponse qrespInitial) {
		List<LetSpan> spanL = buildSpans(queryExp);
		
		//execute span
		QueryResponse qresp = qrespInitial;
		for(LetSpan span: spanL) {
			span.qresp = qresp;
			span.qfeL = adjustExecutionOrder(span);
			qresp = runner.executeSpan(span);
		}
		
		return qresp;
	}
	
	private List<QueryFuncExp> adjustExecutionOrder(LetSpan span) {
		FetchRunner fetchRunner = null;
		
		List<QueryFuncExp> newL = new ArrayList<>();
		
		//do orderby,offset,limit
		for(int passNumber = 1; passNumber <= 3; passNumber++) {
			List<QueryFuncExp> currentList = getPass(span, passNumber, fnFactory);
			newL.addAll(currentList);
		}
		
		//pass 4. fields and other fns
		for(int i = 0; i < span.qfeL.size(); i++) {
			QueryFuncExp qfexp = span.qfeL.get(i);
			if (newL.contains(qfexp)) {
				continue;
			}
			newL.add(qfexp);
		}
		return newL;
	}

	private List<QueryFuncExp> getPass(LetSpan span, int passNum, ZQueryResponseFunctionFactory fnFactory) {
		List<QueryFuncExp> list = new ArrayList<>();
		for(QueryFuncExp qfe: span.qfeL) {
			if (fnFactory.isPassFunction(passNum, qfe.funcName)) {
				list.add(qfe);
			}
		}
		return list;
	}

	private List<LetSpan> buildSpans(QueryExp queryExp) {
		List<LetSpan> spanL = new ArrayList<>();
		LetSpan span = new LetSpan(registry.getType(queryExp.typeName));
		for(int i = 0; i < queryExp.qfelist.size(); i++) {
			QueryFuncExp qfexp = queryExp.qfelist.get(i);
			
			LetSpan possibleNewSpan = endsSpan(span, qfexp);
			if (possibleNewSpan != null) {
				spanL.add(span);
				span = possibleNewSpan;
				span.qfeL.add(qfexp);
			} else {
				span.qfeL.add(qfexp);
			}
		}
		
		if (! span.qfeL.isEmpty()) {
			spanL.add(span);
		}
		
		return spanL;
	}

	private LetSpan endsSpan(LetSpan span, QueryFuncExp qfexp) {
		if (qfexp instanceof QueryFieldExp) {
			QueryFieldExp qff = (QueryFieldExp) qfexp;
			String fieldName = qff.funcName;
			DType fieldType = DValueHelper.findFieldType(span.structType, fieldName);
			DValueHelper.throwIfFieldNotExist("", fieldName, span.structType);
			if (fieldType.isStructShape()) {
				LetSpan newSpan = new LetSpan(fieldType);
				return newSpan;
			}
		}
		return null;
	}
}