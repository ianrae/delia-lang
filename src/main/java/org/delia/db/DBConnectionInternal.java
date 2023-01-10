package org.delia.db;

import java.sql.Connection;

public interface DBConnectionInternal {
	Connection getJdbcConnection();
}
