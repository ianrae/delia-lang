package org.delia.zqueryresponse.function;

import java.util.Arrays;
import java.util.List;

import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.runner.FetchRunner;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.zqueryresponse.ZQueryResponseFunction;

public class ZQueryResponseFunctionFactory extends ServiceBase {
	private FetchRunner fetchRunner;

	public ZQueryResponseFunctionFactory(FactoryService factorySvc, FetchRunner fetchRunner) {
		super(factorySvc);
		this.fetchRunner = fetchRunner;
	}

	public ZQueryResponseFunction create(String fnName, DTypeRegistry registry) {
		switch(fnName) {
		case "min":
			return new ZMinFunction(factorySvc, registry);
		case "max":
			return new ZMaxFunction(factorySvc, registry);
		case "count":
			return new ZCountFunction(registry);
		case "distinct":
			return new ZDistinctFunction(registry);
		case "exists":
			return new ZExistsFunction(registry);
		case "fetch":
			return new ZFetchFunction(registry, fetchRunner);
		case "fks":
			return new ZFKsFunction(registry, factorySvc.getConfigureService());
		case "orderBy":
			return new ZOrderByFunction(registry);
		case "limit":
			return new ZLimitFunction(registry);
		case "offset":
			return new ZOffsetFunction(registry);
		case "first":
			return new ZFirstFunction(registry, true, false);
		case "last":
			return new ZFirstFunction(registry, false, false);
		case "ith":
			return new ZFirstFunction(registry, false, true);
		default:
		{
			String msg = String.format("unknown fn: %s", fnName);
			et.add("unknown-query-function", msg);
			return null;
		}
		}
	}

	public boolean isPassFunction(int passNumber, String fnName) {
		String[] arPass1Fn = { "orderBy"};
		String[] arPass2Fn = { "offset" };
		String[] arPass3Fn = { "limit" };

		List<String> list = null;
		switch(passNumber) {
		case 1:
			list = Arrays.asList(arPass1Fn);
			break;
		case 2:
			list = Arrays.asList(arPass2Fn);
			break;
		case 3:
			list = Arrays.asList(arPass3Fn);
			break;
		default:
			break;
		}
		return list.contains(fnName);
	}

	public DType getResultType(QueryFuncExp qfe, DStructType structType, QueryFuncExp currentField, DTypeRegistry registry) {
		switch(qfe.funcName) {
		case "min":
			return getTypeOfQFE(qfe, structType, currentField, registry);
		case "max":
			return getTypeOfQFE(qfe, structType, currentField, registry);
		case "count":
			return registry.getType(BuiltInTypes.LONG_SHAPE);
		case "distinct":
			return null; //does not change result type
		case "exists":
			return registry.getType(BuiltInTypes.BOOLEAN_SHAPE);
		case "fetch":
		case "fks":
		case "orderBy":
		case "limit":
		case "offset":
			return null; //does not change result type
		case "first":
		case "last":
		case "ith":
			return null; //does not change result type
		default:
		{
			//				String msg = String.format("unknown fn: %s", fnName);
			//				et.add("unknown-query-function", msg);
			return null;
		}
		}
	}

	private DType getTypeOfQFE(QueryFuncExp qfe, DStructType structType, QueryFuncExp currentField, DTypeRegistry registry) {
		String fieldName = currentField.funcName;
		TypePair pair = DValueHelper.findField(structType, fieldName);
		return pair.type;
	}
	private DType getTypeOfArg(QueryFuncExp qfe, DStructType structType, DTypeRegistry registry) {
		String fieldName = qfe.argL.get(0).strValue();
		TypePair pair = DValueHelper.findField(structType, fieldName);
		return pair.type;
	}

}