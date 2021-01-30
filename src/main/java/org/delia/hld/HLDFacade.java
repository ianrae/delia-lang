package org.delia.hld;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.compiler.ast.UpsertStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBType;
import org.delia.db.QuerySpec;
import org.delia.db.SqlStatementGroup;
import org.delia.db.hls.AliasManager;
import org.delia.db.hls.manager.HLSPipelineStep;
import org.delia.db.hls.manager.HLSStragey;
import org.delia.db.hls.manager.InQueryStep;
import org.delia.db.hls.manager.StandardHLSStragey;
import org.delia.hld.cud.HLDDeleteStatement;
import org.delia.hld.cud.HLDInsertStatement;
import org.delia.hld.cud.HLDUpdateStatement;
import org.delia.hld.cud.HLDUpsertStatement;
import org.delia.log.LogLevel;
import org.delia.runner.DValueIterator;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;

/**
 * Facade between Delia Runner and the db. Allows us to have different strategies
 * for executing 'let' statement queries.
 * Normally each query turns into a single SQL call to DBInterface,
 * but some require multiple calls and additional handling.
 * This layer selects a strategy object to execute the query and runs it.
 * @author Ian Rae
 *
 */
public class HLDFacade extends ServiceBase {
	protected DBInterfaceFactory dbInterface;
	protected DTypeRegistry registry;
	protected HLSStragey defaultStrategy = new StandardHLSStragey();
	protected boolean generateSQLforMemFlag;
	protected VarEvaluator varEvaluator;
	protected AliasManager aliasManager;
	protected List<HLSPipelineStep> pipelineL = new ArrayList<>();
	private SprigService sprigSvc; //set after ctor
	private HLDFactory hldFactory;

//	private Delia delia;
	
	public HLDFacade(Delia delia, DTypeRegistry registry, VarEvaluator varEvaluator) {
		super(delia.getFactoryService());
//		this.delia = delia;
		this.dbInterface= delia.getDBInterface();
		this.hldFactory = delia.getHLDFactory();
		this.registry = registry;
		this.varEvaluator = varEvaluator;
		this.aliasManager = new AliasManager(factorySvc);
		this.pipelineL.add(new InQueryStep(factorySvc));
	}
	public HLDFacade(FactoryService factorySvc, DBInterfaceFactory dbInterface, DTypeRegistry registry, VarEvaluator varEvaluator) {
		super(factorySvc);
//		this.session = session;
		this.dbInterface= dbInterface;
		this.hldFactory = dbInterface.getHLDFactory();
		this.registry = registry;
		this.varEvaluator = varEvaluator;
		this.aliasManager = new AliasManager(factorySvc);
		this.pipelineL.add(new InQueryStep(factorySvc));
	}

	public HLDQueryStatement buildQueryStatement(QuerySpec spec, DBExecutor zexec, VarEvaluator varEvaluator) {
		HLDQueryStatement hld = buildHLD(spec.queryExp, zexec, varEvaluator);
		hld.querySpec = spec;
		return hld;
	}

	public HLDQueryStatement buildHLD(QueryExp queryExp, DBExecutor zexec, VarEvaluator varEvaluator) {
		HLDBuildService mgr = createManager(zexec);
		HLDQueryStatement hld = mgr.fullBuildQuery(queryExp, varEvaluator);
		logDebug(hld);
		return hld;
	}
	private void logDebug(HLDStatement hld) {
		if (log.isLevelEnabled(LogLevel.DEBUG)) {
			log.logDebug(hld.toString()); //only do toString if log is enabled
		}
	}
	public boolean canBuildHLD(QueryExp queryExp, DBExecutor zexec, VarEvaluator varEvaluator) {
		HLDBuildService mgr = createManager(zexec);
		return mgr.canBuildQuery(queryExp, varEvaluator);
	}
	public HLDDeleteStatement buildHLD(DeleteStatementExp deleteExp, DBExecutor zexec) {
		QueryExp queryExp = deleteExp.queryExp;
		
		HLDBuildService mgr = createManager(zexec); 
		HLDDeleteStatement hlddel = mgr.fullBuildDelete(queryExp);
		logDebug(hlddel);
		return hlddel;
	}
	public HLDUpdateStatement buildHLD(UpdateStatementExp updateExp, DBExecutor zexec, VarEvaluator varEvaluator, DValueIterator insertPrebuiltValueIterator) {
		HLDBuildService mgr = createManager(zexec); 
		HLDUpdateStatement hldupdate = mgr.fullBuildUpdate(updateExp, varEvaluator, insertPrebuiltValueIterator);
		logDebug(hldupdate);
		return hldupdate;
	}
	public HLDUpsertStatement buildHLD(UpsertStatementExp upsertExp, DBExecutor zexec, VarEvaluator varEvaluator, DValueIterator insertPrebuiltValueIterator) {
		HLDBuildService mgr = createManager(zexec); 
		HLDUpsertStatement hldupsert = mgr.fullBuildUpsert(upsertExp, varEvaluator, insertPrebuiltValueIterator);
		logDebug(hldupsert);
		return hldupsert;
	}
	public HLDInsertStatement buildHLD(InsertStatementExp insertExp, DBExecutor zexec, VarEvaluator varEvaluator2, DValueIterator insertPrebuiltValueIterator) {
		HLDBuildService mgr = createManager(zexec); 
		HLDInsertStatement hldins = mgr.fullBuildInsert(insertExp, varEvaluator2, insertPrebuiltValueIterator);
		logDebug(hldins);
		return hldins;
	}
	
	
	public SqlStatementGroup generateSqlForQuery(HLDQueryStatement hld, DBExecutor zexec) {
		if (! shouldGenerateSql()) {
			return new SqlStatementGroup();
		}

		HLDBuildService mgr = createManager(zexec);
		SqlStatementGroup stmgrp = mgr.generateSql(hld);
		return stmgrp;
	}
	public SqlStatementGroup generateSQL(HLDInsertStatement hldins, DBExecutor zexec) {
		if (! shouldGenerateSql()) {
			return new SqlStatementGroup();
		}
		HLDBuildService mgr = createManager(zexec);
		return mgr.generateSql(hldins);
	}
	private boolean shouldGenerateSql() {
		if (dbInterface.getDBType().equals(DBType.MEM)) {
			return factorySvc.getEnableMEMSqlGenerationFlag();
		}
		return true;
	}
	public SqlStatementGroup generateSQL(HLDDeleteStatement hlddel, DBExecutor zexec) {
		if (! shouldGenerateSql()) {
			return new SqlStatementGroup();
		}
		HLDBuildService mgr = createManager(zexec);
		return mgr.generateSql(hlddel);
	}
	public SqlStatementGroup generateSQL(HLDUpdateStatement hldupd, DBExecutor zexec) {
		if (! shouldGenerateSql()) {
			return new SqlStatementGroup();
		}
		HLDBuildService mgr = createManager(zexec);
		return mgr.generateSql(hldupd);
	}
	public SqlStatementGroup generateSQL(HLDUpsertStatement hldupd, DBExecutor zexec) {
		if (! shouldGenerateSql()) {
			return new SqlStatementGroup();
		}
		HLDBuildService mgr = createManager(zexec);
		return mgr.generateSql(hldupd);
	}
	
	protected HLDBuildService createManager(DBExecutor zexec) {
		HLDBuildService svc = hldFactory.createHLDBuilderService(registry, factorySvc, zexec.getDatIdMap(), sprigSvc, dbInterface.getDBType());
		return svc;
	}


	public boolean isGenerateSQLforMemFlag() {
		return generateSQLforMemFlag;
	}

	public void setGenerateSQLforMemFlag(boolean generateSQLforMemFlag) {
		this.generateSQLforMemFlag = generateSQLforMemFlag;
	}

	public DTypeRegistry getRegistry() {
		return registry;
	}

	public void setVarEvaluator(VarEvaluator varEvaluator) {
		this.varEvaluator = varEvaluator;
	}

	public SprigService getSprigSvc() {
		return sprigSvc;
	}

	public void setSprigSvc(SprigService sprigSvc) {
		this.sprigSvc = sprigSvc;
	}
}