//package org.delia.db.postgres;
//
//import org.delia.assoc.DatIdMap;
//import org.delia.core.FactoryService;
//import org.delia.db.hls.AliasManager;
//import org.delia.db.hls.HLSQuerySpan;
//import org.delia.db.hls.HLSSQLGeneratorImpl;
//import org.delia.db.hls.SQLCreator;
//import org.delia.db.sql.fragment.MiniSelectFragmentParser;
//import org.delia.runner.VarEvaluator;
//
//public class PostgresHLSSQLGeneratorImpl extends HLSSQLGeneratorImpl {
//	public PostgresHLSSQLGeneratorImpl(FactoryService factorySvc, DatIdMap datIdMap,
//			MiniSelectFragmentParser miniSelectParser, VarEvaluator varEvaluator, AliasManager aliasManager) {
//		super(factorySvc, miniSelectParser, varEvaluator, aliasManager, datIdMap);
//	}
//
//
//	@Override
//	protected void doFirst(SQLCreator sc, HLSQuerySpan hlspan) {
//		//no TOP
//	}
//
//	@Override
//	protected void doLast(SQLCreator sc, HLSQuerySpan hlspan) {
//		//no TOP
//	}
//	
//	
//	protected boolean oloNeedsLimit(HLSQuerySpan hlspan) {
//		return hlspan.hasFunction("exists") || hlspan.hasFunction("first") || hlspan.hasFunction("last");
//	}
//
//}
