//package org.delia.db.hls;
//
//import org.delia.compiler.ast.QueryExp;
//import org.delia.core.FactoryService;
//import org.delia.db.QueryBuilderService;
//import org.delia.db.QueryBuilderServiceImpl;
//import org.delia.db.QueryContext;
//import org.delia.db.QuerySpec;
//import org.delia.db.hls.manager.HLSManagerBase;
//import org.delia.db.hls.manager.HLSManagerResult;
//import org.delia.queryresponse.LetSpanEngine;
//import org.delia.runner.DoNothingVarEvaluator;
//import org.delia.runner.QueryResponse;
//import org.delia.runner.VarEvaluator;
//import org.delia.type.DTypeRegistry;
//import org.delia.zdb.ZDBExecutor;
//import org.delia.zdb.ZDBInterfaceFactory;
//
///**
// * Alternative to QueryBuilderService, that executes using HLS
// * @author ian
// *
// */
//public class HLSSimpleQueryService { 
//	private QueryBuilderService innerSvc;
//	private HLSManagerBase hlsManager;
//	private FactoryService factorySvc;
//	
//	public HLSSimpleQueryService(FactoryService factorySvc, ZDBInterfaceFactory dbInterface, DTypeRegistry registry) {
//		this.factorySvc = factorySvc;
//		this.innerSvc = new QueryBuilderServiceImpl(factorySvc);
//		this.hlsManager = new HLSManagerBase(factorySvc, dbInterface, registry, new DoNothingVarEvaluator());
//		hlsManager.setGenerateSQLforMemFlag(true); //TODO:is this only for unit test?
//	}
//
//	public HLSManagerResult execQueryEx(QueryExp queryExp, ZDBExecutor zexec, VarEvaluator varEvaluator) {
//		QuerySpec querySpec = innerSvc.buildSpec(queryExp, varEvaluator);
//		
//		QueryContext qtx = new QueryContext();
//		hlsManager.setVarEvaluator(varEvaluator);
//		qtx.letSpanEngine = new LetSpanEngine(factorySvc, hlsManager.getRegistry()); 
//		HLSManagerResult result = hlsManager.execute(querySpec, qtx, zexec);
//		return result;
//	}
//	public QueryResponse execQuery(QueryExp queryExp, ZDBExecutor zexec) {
//		QuerySpec querySpec = innerSvc.buildSpec(queryExp, new DoNothingVarEvaluator());
//		
//		QueryContext qtx = new QueryContext();
//		qtx.letSpanEngine = new LetSpanEngine(factorySvc, hlsManager.getRegistry()); 
//		HLSManagerResult result = hlsManager.execute(querySpec, qtx, zexec);
//		return result.qresp;
//	}
//
//	public QueryBuilderService getQueryBuilderSvc() {
//		return innerSvc;
//	}
//}