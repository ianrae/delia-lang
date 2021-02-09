package org.delia.db.postgres;

import org.delia.db.DBType;
import org.delia.db.sql.ConnectionString;

public class PostgresConnectionHelper {
	
	public static ConnectionString getTestDB() {
		ConnectionString connStr = new ConnectionString();
		connStr.jdbcUrl = "jdbc:postgresql://localhost/delia";
		
		connStr.dbType = DBType.POSTGRES;
		connStr.userName = "ian";
		connStr.pwd = "admin";
		
		//TODO: add setting for choosing https://jdbc.postgresql.org/documentation/81/ds-ds.html
		org.postgresql.ds.PGSimpleDataSource ds = new org.postgresql.ds.PGSimpleDataSource();
//		org.postgresql.ds.PGPoolingDataSource ds = new org.postgresql.ds.PGPoolingDataSource();
		ds.setUser(connStr.userName);
		ds.setPassword(connStr.pwd);
		ds.setURL(connStr.jdbcUrl);
		connStr.ds = ds;
		
		return connStr;
	}
	
}
