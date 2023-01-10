package org.delia.h2;

import org.delia.db.DBType;
import org.delia.db.sql.ConnectionDefinition;

public class H2ConnectionHelper {
	
	public static boolean usePostgresVariant = false;

	public static ConnectionDefinition getTestDB() {
		ConnectionDefinition connStr = new ConnectionDefinition();
		connStr.dbType = DBType.H2;

		//postgres variant of h2
		if (usePostgresVariant) {
			connStr.jdbcUrl = "jdbc:h2:~/testpgseede;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";
		} else {
			//normal h2
			connStr.jdbcUrl = "jdbc:h2:~/testseede";
		}
		
		connStr.userName = "sa";
		connStr.pwd = "";
		
		org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
		ds.setUser(connStr.userName);
		ds.setPassword(connStr.pwd);
		ds.setURL(connStr.jdbcUrl);
		connStr.ds = ds;
		
		return connStr;
	}
	
}
