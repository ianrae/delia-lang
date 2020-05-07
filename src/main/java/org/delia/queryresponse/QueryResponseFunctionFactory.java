package org.delia.queryresponse;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.runner.FetchRunner;
import org.delia.type.DTypeRegistry;

public class QueryResponseFunctionFactory extends ServiceBase {
	private FetchRunner fetchRunner;

	public QueryResponseFunctionFactory(FactoryService factorySvc, FetchRunner fetchRunner) {
		super(factorySvc);
		this.fetchRunner = fetchRunner;
	}

	public QueryResponseFunction create(String fnName, DTypeRegistry registry) {
//		switch(fnName) {
//		case "min":
//			return new MinFunction(registry);
//		case "max":
//			return new MaxFunction(registry);
//		case "count":
//			return new CountFunction(registry);
//		case "distinct":
//			return new DistinctFunction(registry);
//		case "exist":
//			return new ExistsFunction(registry);
//		case "fetch":
//			return new FetchFunction(registry, fetchRunner);
//		case "fks":
//			return new FKsFunction(registry, factorySvc.getConfigureService());
//		case "orderBy":
//			return new OrderByFunction(registry);
//		case "limit":
//			return new LimitFunction(registry);
//		case "offset":
//			return new OffsetFunction(registry);
//		case "first":
//			return new FirstFunction(registry, true, false);
//		case "last":
//			return new FirstFunction(registry, false, false);
//		case "ith":
//			return new FirstFunction(registry, false, true);
//		default:
//		{
//			String msg = String.format("unknown fn: %s", fnName);
//			et.add("unknown-query-function", msg);
			return null;
//		}
//		}
	}
}