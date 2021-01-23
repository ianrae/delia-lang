//package org.delia.db.hls.manager;
//
//import org.delia.api.Delia;
//import org.delia.api.DeliaSession;
//import org.delia.compiler.ast.QueryExp;
//import org.delia.db.QuerySpec;
//import org.delia.db.hls.HLSQuerySpan;
//import org.delia.db.hls.HLSQueryStatement;
//import org.delia.db.sql.QueryType;
//import org.delia.db.sql.QueryTypeDetector;
//import org.delia.runner.VarEvaluator;
//import org.delia.type.DTypeRegistry;
//
///**
// * Facade between Delia Runner and the db. Allows us to have different strategies
// * for executing 'let' statement queries.
// * Normally each query turns into a single SQL call to DBInterface,
// * but some require multiple calls and additional handling.
// * This layer selects a strategy object to execute the query and runs it.
// * @author Ian Rae
// *
// */
//public class HLSManager extends HLSManagerBase {
//
//	private DeliaSession session;
//	private Delia delia;
//	
//	public HLSManager(Delia delia, DTypeRegistry registry, DeliaSession session, VarEvaluator varEvaluator) {
//		super(delia.getFactoryService(), delia.getDBInterface(), registry, varEvaluator);
//		this.session = session;
//		this.delia = delia;
//	}
//
//	@Override
//	protected HLSStragey chooseStrategy(HLSQueryStatement hls) {
//		if (needDoubleStrategy(hls)) {
//			return new DoubleHLSStragey(delia, session);
//		}
//		return defaultStrategy;
//	}
//
//	private boolean needDoubleStrategy(HLSQueryStatement hls) {
//		if (hls.hlspanL.size() == 2) {
//			//				HLSQuerySpan hlspan1 = hls.hlspanL.get(1); //Address
//			HLSQuerySpan hlspan2 = hls.hlspanL.get(0); //Customer
//
//			QueryType queryType = detectQueryType(hls.queryExp, hlspan2);
//			switch(queryType) {
//			case OP:
//				return true;
//			case ALL_ROWS:
//			case PRIMARY_KEY:
//			default:
//				return false;
//			}
//		} 
//		return false;
//	}
//	private QueryType detectQueryType(QueryExp queryExp, HLSQuerySpan hlspan) {
//		QueryTypeDetector queryTypeDetector = new QueryTypeDetector(factorySvc, registry);
//		QuerySpec spec = new QuerySpec();
//		spec.queryExp = queryExp;
//		QueryType queryType = queryTypeDetector.detectQueryType(spec);
//		return queryType;
//	}
//
//}