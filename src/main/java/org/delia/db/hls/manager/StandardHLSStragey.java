package org.delia.db.hls.manager;

import org.delia.db.QueryContext;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.runner.QueryResponse;
import org.delia.zdb.DBExecutor;

//normally we just call db directly. one 'let' statement = one call to db
public class StandardHLSStragey implements HLSStragey {

	@Override
	public QueryResponse execute(HLSQueryStatement hls, String sql, QueryContext qtx, DBExecutor dbexecutor) {
		QueryResponse qresp = null;// dbexecutor.executeHLSQuery(hls, sql, qtx);
		return qresp;
	}
}