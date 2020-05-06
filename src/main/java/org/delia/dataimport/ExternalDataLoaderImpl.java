package org.delia.dataimport;

import org.delia.api.DeliaSession;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.DBHelper;
import org.delia.db.DBInterface;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.QueryResponse;
import org.delia.runner.inputfunction.ExternalDataLoader;
import org.delia.type.DRelation;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;

public class ExternalDataLoaderImpl extends ServiceBase implements ExternalDataLoader {

	private DeliaSession externalSession;
	private DTypeRegistry externalRegistry;
	private DBInterface externalDBInterface;
	private DoNothingVarEvaluator varEvaluator;

	public ExternalDataLoaderImpl(FactoryService factorySvc, DeliaSession externalSession) {
		super(factorySvc);
		this.externalSession = externalSession;
		this.externalRegistry = externalSession.getExecutionContext().registry;
		this.externalDBInterface = externalSession.getDelia().getDBInterface();
		this.varEvaluator = new DoNothingVarEvaluator(); //TODO fix later. what should we use?
	}

	@Override
	public QueryResponse queryFKsExist(DRelation drel) {
		QuerySpec spec = buildQuery(drel);
		
		QueryResponse qresp = null;
		QueryContext qtx = new QueryContext();
		DBAccessContext dbctx = new DBAccessContext(externalRegistry, varEvaluator);
		try(DBExecutor dbexecutor = externalDBInterface.createExector(dbctx)) {
			qresp = dbexecutor.executeQuery(spec, qtx);
		} catch (Exception e) {
			DBHelper.handleCloseFailure(e);
		}

		return qresp;
	}

	@Override
	public QueryResponse queryObjects(DRelation drel) {
		QuerySpec spec = buildQuery(drel);
		
		QueryResponse qresp = null;
		QueryContext qtx = new QueryContext();
		DBAccessContext dbctx = new DBAccessContext(externalRegistry, varEvaluator);
		try(DBExecutor dbexecutor = externalDBInterface.createExector(dbctx)) {
			qresp = dbexecutor.executeQuery(spec, qtx);
		} catch (Exception e) {
			DBHelper.handleCloseFailure(e);
		}

		return qresp;
	}
	
	private QuerySpec buildQuery(DRelation drel) {
		QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
		QueryExp exp;
		if (drel.isMultipleKey()) {
			DType relType = externalRegistry.getType(drel.getTypeName());
			exp = builderSvc.createInQuery(drel.getTypeName(), drel.getMultipleKeys(), relType);
		} else {
			exp = builderSvc.createPrimaryKeyQuery(drel.getTypeName(), drel.getForeignKey());
		}
		QuerySpec spec = builderSvc.buildSpec(exp, varEvaluator);
		return spec;
	}
}