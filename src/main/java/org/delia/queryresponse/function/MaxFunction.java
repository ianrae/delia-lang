package org.delia.queryresponse.function;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.QueryExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.queryresponse.QueryResponseFunctionBase;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;

public class MaxFunction extends QueryResponseFunctionBase {
	public MaxFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(String fnName, QueryExp queryExp, QueryResponse qresp, QueryFuncContext ctx) {
		List<DValue> dvalList = ctx.getDValList();
		if (CollectionUtils.isEmpty(dvalList)) {
			qresp.dvalList = null;
			return qresp; //min of empty set is null
		}
		
		Shape shape = detectShape(ctx);
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
			DeliaExceptionHelper.throwError("unsupported-max-type", "max() doesn't support type '%s'", shape);
		}
		return qresp;
	}

	private QueryResponse processInt(QueryResponse qresp, List<DValue> dvalList) {
		int max = Integer.MIN_VALUE;
		for(DValue dval: dvalList) {
			if (dval == null) {
				continue;
			}
			int k = dval.asInt(); 
			if (k > max) {
				max = k;
			}
		}
		
		DValue dval = buildIntVal(max);
		setSingletonResult(qresp, dval);
		return qresp;
	}
	private QueryResponse processLong(QueryResponse qresp, List<DValue> dvalList) {
		long max = Integer.MIN_VALUE;
		for(DValue dval: dvalList) {
			if (dval == null) {
				continue;
			}
			long k = dval.asLong(); 
			if (k > max) {
				max = k;
			}
		}
		
		DValue dval = buildLongVal(max);
		setSingletonResult(qresp, dval);
		return qresp;
	}
	private QueryResponse processNumber(QueryResponse qresp, List<DValue> dvalList) {
		double max = Double.MIN_VALUE;
		for(DValue dval: dvalList) {
			if (dval == null) {
				continue;
			}
			double k = dval.asNumber();
			if (k > max) {
				max = k;
			}
		}
		
		DValue dval = buildNumberVal(max);
		setSingletonResult(qresp, dval);
		return qresp;
	}
	private QueryResponse processString(QueryResponse qresp, List<DValue> dvalList) {
		String min = null; //min possible string
		for(DValue dval: dvalList) {
			if (dval == null) {
				continue;
			}
			String k = dval.asString();
			
			if (min == null) {
				min = k;
			} else if (k.compareTo(min) > 0) {
				min = k;
			}
		}
		
		DValue dval = buildStringVal(min);
		setSingletonResult(qresp, dval);
		return qresp;
	}

	/**
	 * may be all null
	 * @param ctx query-fn context
	 * @return shape or null
	 */
	private Shape detectShape(QueryFuncContext ctx) {
		List<DValue> dvalList = ctx.getDValList();
		for(DValue dval: dvalList) {
			if (dval != null) {
				return dval.getType().getShape();
			}
		}
		return null;
	}
}