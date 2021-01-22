package org.delia.db.hls.manager;

import org.delia.db.QueryContext;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.runner.QueryResponse;
import org.delia.zdb.ZDBExecutor;

//normally we just call db directly. one 'let' statement = one call to db
public class StandardHLSStragey implements HLSStragey {

	@Override
	public QueryResponse execute(HLSQueryStatement hls, String sql, QueryContext qtx, ZDBExecutor dbexecutor) {
		QueryResponse qresp = null;// dbexecutor.executeHLSQuery(hls, sql, qtx);
		return qresp;
	}
}