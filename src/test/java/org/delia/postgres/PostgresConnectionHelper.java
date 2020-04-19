package org.delia.postgres;

import org.delia.db.sql.ConnectionString;

public class PostgresConnectionHelper {
	
	public static ConnectionString getTestDB() {
		ConnectionString connStr = new ConnectionString();
		connStr.jdbcUrl = "jdbc:postgresql://localhost/delia";
		
		connStr.userName = "ian";
		connStr.pwd = "admin";
		return connStr;
	}
	
}
