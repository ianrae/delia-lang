package org.delia.db.newhls;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.core.ServiceBase;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.hls.AliasManager;
import org.delia.db.hls.manager.HLSManagerResult;
import org.delia.db.hls.manager.HLSPipelineStep;
import org.delia.db.hls.manager.HLSStragey;
import org.delia.db.hls.manager.InQueryStep;
import org.delia.db.hls.manager.StandardHLSStragey;
import org.delia.db.newhls.cud.HLDDeleteStatement;
import org.delia.db.newhls.cud.HLDInsertStatement;
import org.delia.db.newhls.cud.HLDUpdateStatement;
import org.delia.db.sql.fragment.MiniSelectFragmentParser;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigServiceImpl;
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
	protected MiniSelectFragmentParser miniSelectParser;
	protected AliasManager aliasManager;
	protected List<HLSPipelineStep> pipelineL = new ArrayList<>();

	private DeliaSession session;
	private Delia delia;
	
	public HLDManager(Delia delia, DTypeRegistry registry, DeliaSession session, VarEvaluator varEvaluator) {
		super(delia.getFactoryService());
		this.session = session;
		this.delia = delia;
		this.dbInterface= delia.getDBInterface();
		this.registry = registry;
		this.varEvaluator = varEvaluator;
		this.aliasManager = new AliasManager(factorySvc);
		this.pipelineL.add(new InQueryStep(factorySvc));
	}

	public HLSManagerResult execute(QuerySpec spec, QueryContext qtx, ZDBExecutor zexec) {
		HLDQueryStatement hld = buildHLD(spec.queryExp, zexec);
		hld.querySpec = spec;

		HLDInnerManager mgr = createManager(zexec);
		SqlStatement stm = mgr.generateSql(hld);
		SqlStatementGroup stmgrp = new SqlStatementGroup();
		stmgrp.add(stm);
		
		QueryResponse qresp = zexec.executeHLDQuery(hld, stm.sql, qtx);

		HLSManagerResult result = new HLSManagerResult();
		result.qresp = qresp;
		result.sql = stm.sql;
		return result;
	}

//	private HLSSQLGenerator chooseGenerator(ZDBExecutor zexec) {
//		//later we will have dbspecific ones
//		HLSSQLGenerator gen = new HLSSQLGeneratorImpl(factorySvc, miniSelectParser, varEvaluator, aliasManager, zexec.getDatIdMap());
//		switch(dbInterface.getDBType()) {
//		case MEM:
//		{
//			if (generateSQLforMemFlag) {
//				return new DoNothingSQLGenerator(gen);
//			} else {
//				return new DoNothingSQLGenerator(null);
//			}
//		}
//		case H2:
//			return gen;
//		case POSTGRES:
//			return new PostgresHLSSQLGeneratorImpl(factorySvc, zexec.getDatIdMap(), miniSelectParser, varEvaluator, aliasManager);
//		default:
//			return null; //should never happen
//		}
//	}
	
	public HLDQueryStatement buildHLD(QueryExp queryExp, ZDBExecutor zexec) {
		HLDInnerManager mgr = createManager(zexec);
		HLDQueryStatement hld = mgr.fullBuildQuery(queryExp);
		log.log(hld.toString());
		return hld;
	}
	public HLDDeleteStatement buildHLD(DeleteStatementExp deleteExp, ZDBExecutor zexec) {
		QueryExp queryExp = deleteExp.queryExp;
		
		HLDInnerManager mgr = createManager(zexec); 
		HLDDeleteStatement hlddel = mgr.fullBuildDelete(queryExp);
		log.log(hlddel.toString());
		return hlddel;
	}
	public HLDUpdateStatement buildHLD(UpdateStatementExp updateExp, ZDBExecutor zexec, VarEvaluator varEvaluator) {
		HLDInnerManager mgr = createManager(zexec); 
		HLDUpdateStatement hldupdate = mgr.fullBuildUpdate(updateExp, varEvaluator);
		log.log(hldupdate.toString());
		return hldupdate;
	}
	public HLDInsertStatement buildHLD(InsertStatementExp insertExp, ZDBExecutor zexec, VarEvaluator varEvaluator2) {
		HLDInnerManager mgr = createManager(zexec); 
		HLDInsertStatement hldins = mgr.fullBuildInsert(insertExp, varEvaluator);
		log.log(hldins.toString());
		return hldins;
	}
	public SqlStatementGroup generateSQL(HLDInsertStatement hldins, ZDBExecutor zexec) {
		HLDInnerManager mgr = createManager(zexec);
		return mgr.generateSql(hldins);
	}
	
	protected HLDInnerManager createManager(ZDBExecutor zexec) {
		SprigService sprigSvc = new SprigServiceImpl(factorySvc, registry);
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
}