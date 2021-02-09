package org.delia.db.sql;

import javax.sql.DataSource;

import org.delia.db.DBType;

/**
 * Information required to connect to a database.
 * @author ian
 *
 */
public class ConnectionDefinition {
	public DBType dbType;
	public String userName;
	public String pwd;
	public DataSource ds; //only one of jdbcUrl and ds need to be set. ds takes precedence if both set.
	public String jdbcUrl;

	public ConnectionDefinition() {
	}
	public ConnectionDefinition(DBType dbType, String userName, String pwd, DataSource ds) {
		this.dbType = dbType;
		this.userName = userName;
		this.pwd = pwd;
		this.ds = ds;
		this.jdbcUrl = null;
	}
	public ConnectionDefinition(DBType dbType, String userName, String pwd, String jdbcUrl) {
		this.dbType = dbType;
		this.userName = userName;
		this.pwd = pwd;
		this.ds = null;
		this.jdbcUrl = jdbcUrl;
	}
}
