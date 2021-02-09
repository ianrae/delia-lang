package org.delia.db.h2;

import javax.sql.DataSource;

import org.delia.db.DBType;
import org.delia.db.sql.ConnectionString;

public class H2ConnectionHelper {
	
	public static boolean usePostgresVariant = false;

	public static ConnectionString getTestDB() {
		ConnectionString connStr = new ConnectionString();
		connStr.dbType = DBType.H2;

		//postgres variant of h2
		if (usePostgresVariant) {
			connStr.jdbcUrl = "jdbc:h2:~/testpg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";
		} else {
			//normal h2
			connStr.jdbcUrl = "jdbc:h2:~/test";
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
