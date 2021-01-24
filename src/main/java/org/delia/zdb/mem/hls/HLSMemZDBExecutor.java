//package org.delia.zdb.mem.hls;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import org.delia.core.FactoryService;
//import org.delia.db.hls.GElement;
//import org.delia.db.hls.HLSQuerySpan;
//import org.delia.db.hls.HLSQueryStatement;
//import org.delia.queryfunction.FuncScope;
//import org.delia.queryfunction.QueryFuncContext;
//import org.delia.runner.QueryResponse;
//import org.delia.zdb.mem.MemZDBExecutor;
//import org.delia.zdb.mem.MemZDBInterfaceFactory;
//import org.delia.zdb.mem.hls.function.MemCountFunction;
//import org.delia.zdb.mem.hls.function.MemDistinctFunction;
//import org.delia.zdb.mem.hls.function.MemExistsFunction;
//import org.delia.zdb.mem.hls.function.MemFetchFunction;
//import org.delia.zdb.mem.hls.function.MemFieldFunction;
//import org.delia.zdb.mem.hls.function.MemFirstFunction;
//import org.delia.zdb.mem.hls.function.MemFksFunction;
//import org.delia.zdb.mem.hls.function.MemLimitFunction;
//import org.delia.zdb.mem.hls.function.MemMaxFunction;
//import org.delia.zdb.mem.hls.function.MemMinFunction;
//import org.delia.zdb.mem.hls.function.MemOffsetFunction;
//import org.delia.zdb.mem.hls.function.MemOrderByFunction;
//
///**
// * Note. This now uses HLD layer (not HLS).
// * 
// * Use HLS data to do MEM query
// * @author ian
// *
// */
//public class HLSMemZDBExecutor extends MemZDBExecutor {
//
//	public HLSMemZDBExecutor(FactoryService factorySvc, MemZDBInterfaceFactory dbInterface) {
//		super(factorySvc, dbInterface);
//	}
//
//	
//	
//}