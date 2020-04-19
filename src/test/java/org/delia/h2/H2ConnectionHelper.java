package org.delia.h2;

import org.delia.db.sql.ConnectionString;

public class H2ConnectionHelper {
	
	public static boolean usePostgresVariant = false;

	public static ConnectionString getTestDB() {
		ConnectionString connStr = new ConnectionString();
		//postgres variant of h2
		if (usePostgresVariant) {
			connStr.jdbcUrl = "jdbc:h2:~/testpg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";
		} else {
			//normal h2
			connStr.jdbcUrl = "jdbc:h2:~/test";
		}
		
		connStr.userName = "sa";
		connStr.pwd = "";
		return connStr;
	}
	
}
