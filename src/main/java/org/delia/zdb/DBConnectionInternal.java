package org.delia.zdb;

import java.sql.Connection;

public interface DBConnectionInternal {
	Connection getJdbcConnection();
}
