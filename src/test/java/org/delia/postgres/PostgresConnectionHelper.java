package org.delia.postgres;

import org.delia.db.DBType;
import org.delia.db.sql.ConnectionDefinition;

public class PostgresConnectionHelper {
	
	public static ConnectionDefinition getTestDB() {
		ConnectionDefinition connStr = new ConnectionDefinition();
		connStr.jdbcUrl = "jdbc:postgresql://localhost/delia";
		
		connStr.dbType = DBType.POSTGRES;
		connStr.userName = "postgres";
		connStr.pwd = "somePassword";
		
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
