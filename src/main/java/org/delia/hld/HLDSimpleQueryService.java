package org.delia.hld;

import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryBuilderServiceImpl;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.SqlStatementGroup;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;

/**
 * Alternative to QueryBuilderService, that executes using HLD
 * @author ian
 *
 */
public class HLDSimpleQueryService { 
	private QueryBuilderService innerSvc;
	private HLDManager hldManager;
//	private FactoryService factorySvc;
	
	public HLDSimpleQueryService(FactoryService factorySvc, DBInterfaceFactory dbInterface, DTypeRegistry registry) {
//		this.factorySvc = factorySvc;
		this.innerSvc = new QueryBuilderServiceImpl(factorySvc);
		this.hldManager = new HLDManager(factorySvc, dbInterface, registry, new DoNothingVarEvaluator());
		hldManager.setGenerateSQLforMemFlag(true); //TODO:is this only for unit test?
	}

	public QueryResponse execQueryEx(QueryExp queryExp, DBExecutor zexec, VarEvaluator varEvaluator) {
		QuerySpec querySpec = innerSvc.buildSpec(queryExp, varEvaluator);
		
		QueryContext qtx = new QueryContext();
		hldManager.setVarEvaluator(varEvaluator);
		
		querySpec.queryExp = queryExp;
		HLDQueryStatement hld = hldManager.buildQueryStatement(querySpec, zexec, varEvaluator);
		SqlStatementGroup stgroup = hldManager.generateSqlForQuery(hld, zexec);

		QueryResponse qresp = zexec.executeHLDQuery(hld, stgroup, qtx); //** calll the db **
		return qresp;
	}
	public QueryResponse execQuery(QueryExp queryExp, DBExecutor zexec) {
		QuerySpec querySpec = innerSvc.buildSpec(queryExp, new DoNothingVarEvaluator());
		
		QueryContext qtx = new QueryContext();
		qtx.isSimpleSvc = true;
		querySpec.queryExp = queryExp;
		HLDQueryStatement hld = hldManager.buildQueryStatement(querySpec, zexec, new DoNothingVarEvaluator());
		SqlStatementGroup stgroup = hldManager.generateSqlForQuery(hld, zexec);

		QueryResponse qresp = zexec.executeHLDQuery(hld, stgroup, qtx); //** calll the db **
		return qresp;
	}

	public QueryBuilderService getQueryBuilderSvc() {
		return innerSvc;
	}
}