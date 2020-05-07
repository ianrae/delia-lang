package org.delia.zqueryresponse.function;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zqueryresponse.ZQueryResponseFunctionBase;

public class ZMinFunction extends ZQueryResponseFunctionBase {
	public ZMinFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx) {
		List<DValue> dvalList = qresp.dvalList; 
		if (CollectionUtils.isEmpty(dvalList)) {
			qresp.dvalList = null;
			return qresp; //min of empty set is null
		}
		
		Shape shape = detectShape(dvalList);
		if (shape == null) {
			return qresp;
		}
		
		switch(shape) {
		case INTEGER:
			return processInt(qresp, dvalList);
		case LONG:
			return processLong(qresp, dvalList);
		case NUMBER:
			return processNumber(qresp, dvalList);
		case STRING:
			return processString(qresp, dvalList);
		default:
			DeliaExceptionHelper.throwError("unsupported-min-type", "min() doesn't support type '%s'", shape);
		}
		return qresp;
	}

	private QueryResponse processInt(QueryResponse qresp, List<DValue> dvalList) {
		int min = Integer.MAX_VALUE;
		for(DValue dval: dvalList) {
			if (dval == null) {
				continue;
			}
			int k = dval.asInt(); 
			if (k < min) {
				min = k;
			}
		}
		
		DValue dval = buildIntVal(min);
		setSingletonResult(qresp, dval);
		return qresp;
	}
	private QueryResponse processLong(QueryResponse qresp, List<DValue> dvalList) {
		long min = Long.MAX_VALUE;
		for(DValue dval: dvalList) {
			if (dval == null) {
				continue;
			}
			long k = dval.asLong();
			if (k < min) {
				min = k;
			}
		}
		
		DValue dval = buildLongVal(min);
		setSingletonResult(qresp, dval);
		return qresp;
	}
	private QueryResponse processNumber(QueryResponse qresp, List<DValue> dvalList) {
		double min = Double.MAX_VALUE;
		for(DValue dval: dvalList) {
			if (dval == null) {
				continue;
			}
			double k = dval.asNumber();
			if (k < min) {
				min = k;
			}
		}
		
		DValue dval = buildNumberVal(min);
		setSingletonResult(qresp, dval);
		return qresp;
	}
	private QueryResponse processString(QueryResponse qresp, List<DValue> dvalList) {
		String min = null; //max possible string
		for(DValue dval: dvalList) {
			if (dval == null) {
				continue;
			}
			String k = dval.asString();
			
			if (min == null) {
				min = k;
			} else if (k.compareTo(min) < 0) {
				min = k;
			}
		}
		
		DValue dval = buildStringVal(min);
		setSingletonResult(qresp, dval);
		return qresp;
	}

	/**
	 * may be all null
	 * @param dvalList values
	 * @return shape or null
	 */
	private Shape detectShape(List<DValue> dvalList) {
		for(DValue dval: dvalList) {
			if (dval != null) {
				return dval.getType().getShape();
			}
		}
		return null;
	}
}