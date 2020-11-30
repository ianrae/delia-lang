package org.delia.queryresponse;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFieldExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.queryresponse.function.ZQueryResponseFunctionFactory;
import org.delia.runner.QueryResponse;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;

public class LetSpanEngine extends ServiceBase {
	private DTypeRegistry registry;
	private ZQueryResponseFunctionFactory fnFactory;

	public LetSpanEngine(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		
		this.fnFactory = new ZQueryResponseFunctionFactory(factorySvc, null); //fetchRunner not needed here
	}
	
	public QueryResponse processVarRef(QueryExp queryExp, QueryResponse qrespInitial, LetSpanRunner runner) {
		if (qrespInitial.emptyResults()) {
			return qrespInitial;
		}
		
		DValue dval = qrespInitial.dvalList.get(0);
		DType dtype = dval.getType();
		List<LetSpan> spanL = buildSpans(queryExp, dtype);
		
		//execute span
		QueryResponse qresp = qrespInitial;
		for(LetSpan span: spanL) {
			span.qresp = qresp;
			span.qfeL = adjustExecutionOrder(span);
//			qresp = runner.executeSpan(span);
		}
		
		return qresp;
	}
	public QueryResponse process(QueryExp queryExp, QueryResponse qrespInitial, LetSpanRunner runner) {
		DType dtype = registry.getType(queryExp.typeName);
		List<LetSpan> spanL = buildSpans(queryExp, dtype);
		
		//execute span
		QueryResponse qresp = qrespInitial;
		for(LetSpan span: spanL) {
			span.qresp = qresp;
			span.qfeL = adjustExecutionOrder(span);
			qresp = runner.executeSpan(span);
		}
		
		return qresp;
	}
	public List<LetSpan> buildAllSpans(QueryExp queryExp) {
		DType dtype = registry.getType(queryExp.typeName);
		if (dtype == null) {
			//delia src error
			return new ArrayList<LetSpan>();
		}
		
		List<LetSpan> spanL = buildSpans(queryExp, dtype);
		for(LetSpan span: spanL) {
			span.qfeL = adjustExecutionOrder(span);
		}
		return spanL;
	}
	
	private List<QueryFuncExp> adjustExecutionOrder(LetSpan span) {
		List<QueryFuncExp> newL = new ArrayList<>();
		
		if (span.startsWithScopeChange) {
			for(int i = 0; i < span.qfeL.size(); i++) {
				QueryFuncExp qfexp = span.qfeL.get(i);
				if (qfexp instanceof QueryFieldExp) {
					newL.add(qfexp);
					break;
				}
			}
		}
		
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

	private List<LetSpan> buildSpans(QueryExp queryExp, DType dtype) {
		List<LetSpan> spanL = new ArrayList<>();
		LetSpan span = new LetSpan(dtype);
		for(int i = 0; i < queryExp.qfelist.size(); i++) {
			QueryFuncExp qfexp = queryExp.qfelist.get(i);
			
			LetSpan possibleNewSpan = endsSpan(span, qfexp);
			if (possibleNewSpan != null) {
				if (! span.qfeL.isEmpty()) {
					spanL.add(span);
				}
				
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
			if (! span.dtype.isStructShape()) {
				return null;
			}
			DType fieldType = DValueHelper.findFieldType(span.dtype, fieldName);
			DValueHelper.throwIfFieldNotExist("", fieldName, span.dtype);
			if (fieldType.isStructShape()) {
				LetSpan newSpan = new LetSpan(fieldType);
				newSpan.startsWithScopeChange = true;
				return newSpan;
			}
		}
		return null;
	}

	public boolean containsFKs(QueryExp queryExp) {
		for(int i = 0; i < queryExp.qfelist.size(); i++) {
			QueryFuncExp qfexp = queryExp.qfelist.get(i);
			if (qfexp instanceof QueryFieldExp) {    
			} else if (qfexp.funcName.equals("fks")) {
				return true;
			}
		}
		
		return false;
	}
}