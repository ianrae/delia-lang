package org.delia.builder;

import org.delia.db.DBType;
import org.delia.db.sql.ConnectionString;

/**
 * Builds the details of a JDBC connection.
 * 
 * @author Ian Rae
 *
 */
public class ConnectionBuilder {
	private static ConnectionBuilder theSingleton;
	private String jdbcUrl;
	private DBType dbType;
	private String userName;
	private String password;
	
	public static ConnectionBuilder dbType(DBType dbType) {
		theSingleton = new ConnectionBuilder();
		theSingleton.dbType = dbType;
		return theSingleton;
	}
	public ConnectionBuilder jdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
		return this;
	}
	public ConnectionBuilder userName(String userName) {
		this.userName = userName;
		return this;
	}
	public ConnectionBuilder password(String password) {
		this.password = password;
		return this;
	}
	public ConnectionBuilder connectionString(ConnectionString connStr) {
		this.jdbcUrl = connStr.jdbcUrl;
		this.userName = connStr.userName;
		this.password = connStr.pwd;
		return this;
	}
	
	public ConnectionInfo build() {
		ConnectionInfo info = new ConnectionInfo();
		info.dbType = dbType;
		info.jdbcUrl = jdbcUrl;
		info.password = password;
		info.userName = userName;
		return info;
	}
}
