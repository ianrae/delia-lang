package org.delia.db.hls.manager;

import org.delia.db.QueryContext;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.runner.QueryResponse;
import org.delia.zdb.ZDBExecutor;

public interface HLSStragey {
	QueryResponse execute(HLSQueryStatement hls, String sql, QueryContext qtx, ZDBExecutor dbexecutor);
}