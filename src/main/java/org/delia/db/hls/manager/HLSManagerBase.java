package org.delia.db.hls.manager;

import java.util.ArrayList;
import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBType;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.hls.AliasManager;
import org.delia.db.hls.HLSEngine;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.hls.HLSSQLGenerator;
import org.delia.db.hls.HLSSQLGeneratorImpl;
import org.delia.db.postgres.PostgresHLSSQLGeneratorImpl;
import org.delia.db.postgres.PostgresWhereFragmentGenerator;
import org.delia.db.sql.fragment.MiniSelectFragmentParser;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.queryresponse.LetSpan;
import org.delia.queryresponse.LetSpanEngine;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;

/**
 * A lower-level HLS Manager that doesn't need Delia or DeliaSession.
 * @author Ian Rae
 *
 */
public class HLSManagerBase extends ServiceBase {

	protected ZDBInterfaceFactory dbInterface;
	protected DTypeRegistry registry;
	protected HLSStragey defaultStrategy = new StandardHLSStragey();
	protected boolean generateSQLforMemFlag;
	protected VarEvaluator varEvaluator;
	protected MiniSelectFragmentParser miniSelectParser;
	protected AliasManager aliasManager;
	protected List<HLSPipelineStep> pipelineL = new ArrayList<>();
	
	public HLSManagerBase(FactoryService factorySvc, ZDBInterfaceFactory dbInterface, DTypeRegistry registry, VarEvaluator varEvaluator) {
		super(factorySvc);
		this.dbInterface= dbInterface;
		this.registry = registry;
		this.varEvaluator = varEvaluator;
		this.aliasManager = new AliasManager(factorySvc);
		this.pipelineL.add(new InQueryStep(factorySvc));
	}
	
	private void initMiniParser(DatIdMap datIdMap) {
		WhereFragmentGenerator whereGen = createWhereGen(datIdMap);
		this.miniSelectParser = new MiniSelectFragmentParser(factorySvc, registry, whereGen, null);
		whereGen.tableFragmentMaker = miniSelectParser;
	}
	
	private WhereFragmentGenerator createWhereGen(DatIdMap datIdMap) {
		DBType dbType = dbInterface.getDBType();
		switch(dbType) {
		case POSTGRES:
			return new PostgresWhereFragmentGenerator(factorySvc, registry, varEvaluator, datIdMap);
		default:
			return new WhereFragmentGenerator(factorySvc, registry, varEvaluator, datIdMap);
		}
	}
	
	public HLSManagerResult execute(QuerySpec spec, QueryContext qtx, ZDBExecutor zexec) {
		initMiniParser(zexec.getDatIdMap());
		HLSQueryStatement hls = buildHLS(spec.queryExp, zexec.getDatIdMap());
		hls.querySpec = spec;

		HLSSQLGenerator sqlGenerator = chooseGenerator(zexec);
		sqlGenerator.setRegistry(registry);
		String sql = sqlGenerator.buildSQL(hls);

		HLSStragey strategy = chooseStrategy(hls);
		QueryResponse qresp = strategy.execute(hls, sql, qtx, zexec);

		HLSManagerResult result = new HLSManagerResult();
		result.qresp = qresp;
		result.sql = sql;
		return result;
	}

	private HLSSQLGenerator chooseGenerator(ZDBExecutor zexec) {
		//later we will have dbspecific ones
		HLSSQLGenerator gen = new HLSSQLGeneratorImpl(factorySvc, miniSelectParser, varEvaluator, aliasManager, zexec.getDatIdMap());
		switch(dbInterface.getDBType()) {
		case MEM:
		{
			if (generateSQLforMemFlag) {
				return new DoNothingSQLGenerator(gen);
			} else {
				return new DoNothingSQLGenerator(null);
			}
		}
		case H2:
			return gen;
		case POSTGRES:
			return new PostgresHLSSQLGeneratorImpl(factorySvc, zexec.getDatIdMap(), miniSelectParser, varEvaluator, aliasManager);
		default:
			return null; //should never happen
		}
	}

	protected HLSStragey chooseStrategy(HLSQueryStatement hls) {
		return defaultStrategy;
	}

	public HLSQueryStatement buildHLS(QueryExp queryExp, DatIdMap datIdMap) {
		//Warning. sometimes datIdMap can be null (when we are be called by HLSSimpleQueryService)
		LetSpanEngine letEngine = new LetSpanEngine(factorySvc, registry); 
		List<LetSpan> spanL = letEngine.buildAllSpans(queryExp);

		HLSEngine hlsEngine = new HLSEngine(factorySvc, registry);
		HLSQueryStatement hls = hlsEngine.generateStatement(queryExp, spanL);
		//add sanity check
		for (HLSQuerySpan tmp: hls.hlspanL) {
			if (tmp.mainStructType == null) { //failed to find type. delia src error
				return hls;
			}
		}
		hls = runPipeline(hls, queryExp, datIdMap);

		for(HLSQuerySpan hlspan: hls.hlspanL) {
			aliasManager.buildAliases(hlspan, datIdMap);
		}
		for(HLSQuerySpan hlspan: hls.hlspanL) {
			String hlstr = hlspan.toString();
			log.log(hlstr);
		}
		log.log("alias: %s", aliasManager.dumpToString());
		return hls;
	}

	private HLSQueryStatement runPipeline(HLSQueryStatement hls, QueryExp queryExp, DatIdMap datIdMap) {
		for(HLSPipelineStep step: pipelineL) {
			hls = step.execute(hls, queryExp, datIdMap);
		}
		return hls;
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
}