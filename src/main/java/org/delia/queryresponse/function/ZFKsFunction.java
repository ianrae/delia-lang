package org.delia.queryresponse.function;

import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.ConfigureService;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.queryresponse.ZQueryResponseFunctionBase;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;

public class ZFKsFunction extends ZQueryResponseFunctionBase {

	private ConfigureService configSvc;

	public ZFKsFunction(DTypeRegistry registry, ConfigureService configSvc) {
		super(registry);
		this.configSvc = configSvc;
	}

	@Override
	public QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx) {
		configSvc.setPopulateFKsFlag(true);
		return qresp;
	}

}