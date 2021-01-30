package org.delia.zdb.mem.hld.function;

import org.delia.core.FactoryService;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.hld.QueryFnSpec;
import org.delia.queryfunction.QueryFuncContext;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.validation.ValidationRunner;
import org.delia.zdb.mem.MemDBInterfaceFactory;
import org.delia.zdb.mem.hld.MemFunctionBase;

public class MemFksFunction extends MemFunctionBase {
	private FetchRunner fetchRunner;
	private FactoryService factorySvc;
	private MemDBInterfaceFactory dbInterface;
	
	public MemFksFunction(DTypeRegistry registry, FactoryService factorySvc, FetchRunner fetchRunner, MemDBInterfaceFactory dbInterface) {
		super(registry);
		this.factorySvc = factorySvc;
		this.fetchRunner = fetchRunner;
		this.dbInterface = dbInterface;
	}

	@Override
	public QueryResponse process(HLSQuerySpan hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		for(DValue dval: qresp.dvalList) {
			ValidationRunner ruleRunner = factorySvc.createValidationRunner(dbInterface, fetchRunner);
			ruleRunner.enableRelationModifier(true);
			ruleRunner.setPopulateFKsFlag(true);
			ruleRunner.validateRelationRules(dval);
		}
		
		return qresp;
	}
	@Override
	public QueryResponse process(QueryFnSpec hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		HLSQuerySpan jj = null;
		return process(jj, qresp, ctx);
	}


}