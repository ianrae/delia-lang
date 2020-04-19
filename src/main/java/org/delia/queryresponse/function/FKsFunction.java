package org.delia.queryresponse.function;

import org.delia.compiler.ast.QueryExp;
import org.delia.core.ConfigureService;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.queryresponse.QueryResponseFunctionBase;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;

public class FKsFunction extends QueryResponseFunctionBase {

	private ConfigureService configSvc;

	public FKsFunction(DTypeRegistry registry, ConfigureService configSvc) {
		super(registry);
		this.configSvc = configSvc;
	}

	@Override
	public QueryResponse process(String fnName, QueryExp queryExp, QueryResponse qresp, QueryFuncContext ctx) {
		configSvc.setPopulateFKsFlag(true);
		return qresp;
	}

}