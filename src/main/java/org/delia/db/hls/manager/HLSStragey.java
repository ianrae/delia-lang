package org.delia.db.hls.manager;

import org.delia.db.DBExecutor;
import org.delia.db.QueryContext;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.runner.QueryResponse;

public interface HLSStragey {
	QueryResponse execute(HLSQueryStatement hls, String sql, QueryContext qtx, DBExecutor dbexecutor);
}