package org.delia.zqueryresponse.function;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zqueryresponse.ZQueryResponseFunctionBase;

public class ZMaxFunction extends ZQueryResponseFunctionBase {
	private FactoryService factorySvc;

	public ZMaxFunction(FactoryService factorySvc, DTypeRegistry registry) {
		super(registry);
		this.factorySvc = factorySvc;
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
		case BOOLEAN:
			return processBoolean(qresp, dvalList);
		case STRING:
			return processString(qresp, dvalList);
		case DATE:
			return processDate(qresp, dvalList);
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
	private QueryResponse processBoolean(QueryResponse qresp, List<DValue> dvalList) {
		Boolean max = false;
		for(DValue dval: dvalList) {
			if (dval == null) {
				continue;
			}
			Boolean k = dval.asBoolean(); 
			if (k.compareTo(max) > 0) {
				max = k;
			}
		}
		
		DValue dval = buildBoolVal(max);
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
	private QueryResponse processDate(QueryResponse qresp, List<DValue> dvalList) {
		Date min = new Date(Long.MIN_VALUE);
		for(DValue dval: dvalList) {
			if (dval == null) {
				continue;
			}
			Date k = dval.asDate();
			
			if (min == null) {
				min = k;
			} else if (k.compareTo(min) > 0) {
				min = k;
			}
		}
		
		DValue dval = buildDateVal(min, factorySvc);
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