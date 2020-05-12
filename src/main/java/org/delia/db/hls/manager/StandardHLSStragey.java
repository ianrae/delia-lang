package org.delia.db.hls.manager;

import org.delia.db.DBExecutor;
import org.delia.db.QueryContext;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.runner.QueryResponse;

//normally we just call db directly. one 'let' statement = one call to db
public class StandardHLSStragey implements HLSStragey {

	@Override
	public QueryResponse execute(HLSQueryStatement hls, String sql, QueryContext qtx, DBExecutor dbexecutor) {
		QueryResponse qresp = dbexecutor.executeHLSQuery(hls, sql, qtx);
		return qresp;
	}
}