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
import org.delia.runner.DValueIterator;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;

/**
 * Facade between Delia Runner and the db. Allows us to have different strategies
 * for executing 'let' statement queries.
 * Normally each query turns into a single SQL call to DBInterface,
 * but some require multiple calls and additional handling.
 * This layer selects a strategy object to execute the query and runs it.
 * @author Ian Rae
 *
 */
public class HLDManager extends ServiceBase {
	protected ZDBInterfaceFactory dbInterface;
	protected DTypeRegistry registry;
	protected HLSStragey defaultStrategy = new StandardHLSStragey();
	protected boolean generateSQLforMemFlag;
	protected VarEvaluator varEvaluator;
	protected AliasManager aliasManager;
	protected List<HLSPipelineStep> pipelineL = new ArrayList<>();
	private SprigService sprigSvc; //set after ctor

//	private Delia delia;
	
	public HLDManager(Delia delia, DTypeRegistry registry, VarEvaluator varEvaluator) {
		super(delia.getFactoryService());
//		this.delia = delia;
		this.dbInterface= delia.getDBInterface();
		this.registry = registry;
		this.varEvaluator = varEvaluator;
		this.aliasManager = new AliasManager(factorySvc);
		this.pipelineL.add(new InQueryStep(factorySvc));
	}
	public HLDManager(FactoryService factorySvc, ZDBInterfaceFactory dbInterface, DTypeRegistry registry, VarEvaluator varEvaluator) {
		super(factorySvc);
//		this.session = session;
		this.dbInterface= dbInterface;
		this.registry = registry;
		this.varEvaluator = varEvaluator;
		this.aliasManager = new AliasManager(factorySvc);
		this.pipelineL.add(new InQueryStep(factorySvc));
	}

	public HLDQueryStatement buildQueryStatement(QuerySpec spec, ZDBExecutor zexec, VarEvaluator varEvaluator) {
		HLDQueryStatement hld = buildHLD(spec.queryExp, zexec, varEvaluator);
		hld.querySpec = spec;
		return hld;
	}
	public SqlStatementGroup generateSqlForQuery(HLDQueryStatement hld, ZDBExecutor zexec) {
		HLDInnerManager mgr = createManager(zexec);
		SqlStatementGroup stmgrp = mgr.generateSql(hld);
		return stmgrp;
	}

	public HLDQueryStatement buildHLD(QueryExp queryExp, ZDBExecutor zexec, VarEvaluator varEvaluator) {
		HLDInnerManager mgr = createManager(zexec);
		HLDQueryStatement hld = mgr.fullBuildQuery(queryExp, varEvaluator);
		log.log(hld.toString());
		return hld;
	}
	public boolean canBuildHLD(QueryExp queryExp, ZDBExecutor zexec, VarEvaluator varEvaluator) {
		HLDInnerManager mgr = createManager(zexec);
		return mgr.canBuildQuery(queryExp, varEvaluator);
	}
	public HLDDeleteStatement buildHLD(DeleteStatementExp deleteExp, ZDBExecutor zexec) {
		QueryExp queryExp = deleteExp.queryExp;
		
		HLDInnerManager mgr = createManager(zexec); 
		HLDDeleteStatement hlddel = mgr.fullBuildDelete(queryExp);
		log.log(hlddel.toString());
		return hlddel;
	}
	public HLDUpdateStatement buildHLD(UpdateStatementExp updateExp, ZDBExecutor zexec, VarEvaluator varEvaluator, DValueIterator insertPrebuiltValueIterator) {
		HLDInnerManager mgr = createManager(zexec); 
		HLDUpdateStatement hldupdate = mgr.fullBuildUpdate(updateExp, varEvaluator, insertPrebuiltValueIterator);
		log.log(hldupdate.toString());
		return hldupdate;
	}
	public HLDUpsertStatement buildHLD(UpsertStatementExp upsertExp, ZDBExecutor zexec, VarEvaluator varEvaluator, DValueIterator insertPrebuiltValueIterator) {
		HLDInnerManager mgr = createManager(zexec); 
		HLDUpsertStatement hldupsert = mgr.fullBuildUpsert(upsertExp, varEvaluator, insertPrebuiltValueIterator);
		log.log(upsertExp.toString());
		return hldupsert;
	}
	public HLDInsertStatement buildHLD(InsertStatementExp insertExp, ZDBExecutor zexec, VarEvaluator varEvaluator2, DValueIterator insertPrebuiltValueIterator) {
		HLDInnerManager mgr = createManager(zexec); 
		HLDInsertStatement hldins = mgr.fullBuildInsert(insertExp, varEvaluator2, insertPrebuiltValueIterator);
		log.log(hldins.toString());
		return hldins;
	}
	public SqlStatementGroup generateSQL(HLDInsertStatement hldins, ZDBExecutor zexec) {
		HLDInnerManager mgr = createManager(zexec);
		return mgr.generateSql(hldins);
	}
	public SqlStatementGroup generateSQL(HLDDeleteStatement hlddel, ZDBExecutor zexec) {
		HLDInnerManager mgr = createManager(zexec);
		return mgr.generateSql(hlddel);
	}
	public SqlStatementGroup generateSQL(HLDUpdateStatement hldupd, ZDBExecutor zexec) {
		HLDInnerManager mgr = createManager(zexec);
		return mgr.generateSql(hldupd);
	}
	public SqlStatementGroup generateSQL(HLDUpsertStatement hldupd, ZDBExecutor zexec) {
		HLDInnerManager mgr = createManager(zexec);
		return mgr.generateSql(hldupd);
	}
	
	protected HLDInnerManager createManager(ZDBExecutor zexec) {
		return new HLDInnerManager(registry, factorySvc, zexec.getDatIdMap(), sprigSvc);
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