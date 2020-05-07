package org.delia.zqueryresponse.function;

import java.util.Arrays;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.runner.FetchRunner;
import org.delia.type.DTypeRegistry;
import org.delia.zqueryresponse.ZQueryResponseFunction;

public class ZQueryResponseFunctionFactory extends ServiceBase {
		private FetchRunner fetchRunner;

		public ZQueryResponseFunctionFactory(FactoryService factorySvc, FetchRunner fetchRunner) {
			super(factorySvc);
			this.fetchRunner = fetchRunner;
		}

		public ZQueryResponseFunction create(String fnName, DTypeRegistry registry) {
			switch(fnName) {
//			case "min":
//				return new MinFunction(registry);
//			case "max":
//				return new MaxFunction(registry);
			case "count":
				return new ZCountFunction(registry);
			case "distinct":
				return new ZDistinctFunction(registry);
			case "exists":
				return new ZExistsFunction(registry);
//			case "fetch":
//				return new FetchFunction(registry, fetchRunner);
			case "fks":
				return new ZFKsFunction(registry, factorySvc.getConfigureService());
			case "orderBy":
				return new ZOrderByFunction(registry);
//			case "limit":
//				return new LimitFunction(registry);
			case "offset":
				return new ZOffsetFunction(registry);
//			case "first":
//				return new FirstFunction(registry, true, false);
//			case "last":
//				return new FirstFunction(registry, false, false);
//			case "ith":
//				return new FirstFunction(registry, false, true);
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
	}