package org.delia.db.sql;

import javax.sql.DataSource;

import org.delia.db.DBType;

public class ConnectionString {
	public DBType dbType;
	public String userName;
	public String pwd;
	public DataSource ds; //only one of jdbcUrl and ds need to be set. ds takes precedence if both set.
	public String jdbcUrl;

	public ConnectionString() {
	}
	public ConnectionString(DBType dbType, String userName, String pwd, DataSource ds) {
		this.dbType = dbType;
		this.userName = userName;
		this.pwd = pwd;
		this.ds = ds;
		this.jdbcUrl = null;
	}
	public ConnectionString(DBType dbType, String userName, String pwd, String jdbcUrl) {
		this.dbType = dbType;
		this.userName = userName;
		this.pwd = pwd;
		this.ds = null;
		this.jdbcUrl = jdbcUrl;
	}
}
