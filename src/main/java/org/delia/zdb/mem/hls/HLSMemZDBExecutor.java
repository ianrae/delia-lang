package org.delia.zdb.mem.hls;

import org.delia.core.FactoryService;
import org.delia.db.QueryContext;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.runner.QueryResponse;
import org.delia.zdb.mem.MemZDBExecutor;
import org.delia.zdb.mem.MemZDBInterfaceFactory;

/**
 * Use HLS data to do MEM query
 * @author ian
 *
 */
public class HLSMemZDBExecutor extends MemZDBExecutor {

	public HLSMemZDBExecutor(FactoryService factorySvc, MemZDBInterfaceFactory dbInterface) {
		super(factorySvc, dbInterface);
	}

	@Override
	public QueryResponse executeHLSQuery(HLSQueryStatement hls, String sql, QueryContext qtx) {
		log.log("ziggy!");
		qtx.pruneParentRelationFlag = false;
		qtx.loadFKs = false;
		QueryResponse qresp = doExecuteQuery(hls.querySpec, qtx);
		
		//do all spans after first
		for(int i = 1; i < hls.hlspanL.size(); i++) {
			HLSQuerySpan hlspan = hls.hlspanL.get(i);
		}
		
		return qresp;
	}
}