package org.delia.db.hls.manager;

import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.ServiceBase;
import org.delia.db.DBExecutor;
import org.delia.db.DBInterface;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.hls.AssocTblManager;
import org.delia.db.hls.HLSEngine;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.hls.HLSSQLGenerator;
import org.delia.db.hls.HLSSQLGeneratorImpl;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.zqueryresponse.LetSpan;
import org.delia.zqueryresponse.LetSpanEngine;

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
	private DBInterface dbInterface;
	private DTypeRegistry registry;
	private AssocTblManager assocTblMgr;
	private HLSStragey defaultStrategy = new StandardHLSStragey();
	private boolean generateSQLforMemFlag;
	private Delia delia;

	public HLSManager(Delia delia, DTypeRegistry registry, DeliaSession session) {
		super(delia.getFactoryService());
		this.session = session;
		this.delia = delia;
		this.dbInterface= delia.getDBInterface();
		this.registry = registry;
		this.assocTblMgr = new AssocTblManager();
	}

	public HLSManagerResult execute(QuerySpec spec, QueryContext qtx, DBExecutor dbexecutor) {
		HLSQueryStatement hls = buildHLS(spec.queryExp);
		hls.querySpec = spec;

		HLSSQLGenerator sqlGenerator = chooseGenerator();
		sqlGenerator.setRegistry(registry);
		String sql = sqlGenerator.buildSQL(hls);

		HLSStragey strategy = chooseStrategy(hls);
		QueryResponse qresp = strategy.execute(hls, sql, qtx, dbexecutor);

		HLSManagerResult result = new HLSManagerResult();
		result.qresp = qresp;
		result.sql = sql;
		return result;
	}

	private HLSSQLGenerator chooseGenerator() {
		//later we will have dbspecific ones

		HLSSQLGenerator gen = new HLSSQLGeneratorImpl(factorySvc, this.assocTblMgr);
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
		case POSTGRES:
		default:
			return gen;
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


	public HLSQueryStatement buildHLS(QueryExp queryExp) {
		LetSpanEngine letEngine = new LetSpanEngine(factorySvc, registry, null, null); //TODO what are these nulls?
		List<LetSpan> spanL = letEngine.buildAllSpans(queryExp);

		HLSEngine hlsEngine = new HLSEngine(factorySvc, registry);
		HLSQueryStatement hls = hlsEngine.generateStatement(queryExp, spanL);

		for(HLSQuerySpan hlspan: hls.hlspanL) {
			String hlstr = hlspan.toString();
			log.log(hlstr);
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