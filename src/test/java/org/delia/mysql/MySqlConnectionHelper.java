package org.delia.mysql;

import org.delia.db.DBType;
import org.delia.db.sql.ConnectionDefinition;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//https://www.baeldung.com/java-connect-mysql
public class MySqlConnectionHelper {
	
	public static ConnectionDefinition getTestDB() {
		ConnectionDefinition connStr = new ConnectionDefinition();
		connStr.jdbcUrl = "jdbc:mysql://localhost:3306/delia";
		
		connStr.dbType = DBType.MYSQL;
		connStr.userName = "ezclockerMaster";
		connStr.pwd = "41d70562.4400_43";

//        try {
//            Connection z = DriverManager.getConnection("sdf");
//			z.sou
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//        //TODO: add setting for choosing https://jdbc.postgresql.org/documentation/81/ds-ds.html
//		org.postgresql.ds.PGSimpleDataSource ds = new org.postgresql.ds.PGSimpleDataSource();
////		org.postgresql.ds.PGPoolingDataSource ds = new org.postgresql.ds.PGPoolingDataSource();
//		ds.setUser(connStr.userName);
//		ds.setPassword(connStr.pwd);
//		ds.setURL(connStr.jdbcUrl);
//		connStr.ds = ds;
		
		return connStr;
	}
	
}
