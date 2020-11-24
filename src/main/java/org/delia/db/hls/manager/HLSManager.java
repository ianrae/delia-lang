package org.delia.db.hls.manager;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.QueryExp;
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
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
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
 * Facade between Delia Runner and the db. Allows us to have different strategies
 * for executing 'let' statement queries.
 * Normally each query turns into a single SQL call to DBInterface,
 * but some require multiple calls and additional handling.
 * This layer selects a strategy object to execute the query and runs it.
 * @author Ian Rae
 *
 */
public class HLSManager extends ServiceBase {

	private DeliaSession session;
	private ZDBInterfaceFactory dbInterface;
	private DTypeRegistry registry;
	private HLSStragey defaultStrategy = new StandardHLSStragey();
	private boolean generateSQLforMemFlag;
	private Delia delia;
	private VarEvaluator varEvaluator;
	private MiniSelectFragmentParser miniSelectParser;
	private AliasManager aliasManager;
	private List<HLSPipelineStep> pipelineL = new ArrayList<>();
	
	public HLSManager(Delia delia, DTypeRegistry registry, DeliaSession session, VarEvaluator varEvaluator) {
		super(delia.getFactoryService());
		this.session = session;
		this.delia = delia;
		this.dbInterface= delia.getDBInterface();
		this.registry = registry;
		this.varEvaluator = varEvaluator;
		this.aliasManager = new AliasManager(factorySvc);
		this.pipelineL.add(new InQueryStep(factorySvc));
	}
	
	private void initMiniParser(DatIdMap datIdMap) {
		WhereFragmentGenerator whereGen = createWhereGen(datIdMap);
		this.miniSelectParser = new MiniSelectFragmentParser(factorySvc, registry, whereGen);
		whereGen.tableFragmentMaker = miniSelectParser;
	}
	
	private WhereFragmentGenerator createWhereGen(DatIdMap datIdMap) {
		DBType dbType = delia.getDBInterface().getDBType();
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

	private HLSStragey chooseStrategy(HLSQueryStatement hls) {
		if (needDoubleStrategy(hls)) {
			return new DoubleHLSStragey(delia, session);
		}
		return defaultStrategy;
	}

	private boolean needDoubleStrategy(HLSQueryStatement hls) {
		if (hls.hlspanL.size() == 2) {
			//				HLSQuerySpan hlspan1 = hls.hlspanL.get(1); //Address
			HLSQuerySpan hlspan2 = hls.hlspanL.get(0); //Customer

			QueryType queryType = detectQueryType(hls.queryExp, hlspan2);
			switch(queryType) {
			case OP:
				return true;
			case ALL_ROWS:
			case PRIMARY_KEY:
			default:
				return false;
			}
		} 
		return false;
	}
	private QueryType detectQueryType(QueryExp queryExp, HLSQuerySpan hlspan) {
		QueryTypeDetector queryTypeDetector = new QueryTypeDetector(factorySvc, registry);
		QuerySpec spec = new QuerySpec();
		spec.queryExp = queryExp;
		QueryType queryType = queryTypeDetector.detectQueryType(spec);
		return queryType;
	}


	public HLSQueryStatement buildHLS(QueryExp queryExp, DatIdMap datIdMap) {
		LetSpanEngine letEngine = new LetSpanEngine(factorySvc, registry); 
		List<LetSpan> spanL = letEngine.buildAllSpans(queryExp);

		HLSEngine hlsEngine = new HLSEngine(factorySvc, registry);
		HLSQueryStatement hls = hlsEngine.generateStatement(queryExp, spanL);
		
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
}