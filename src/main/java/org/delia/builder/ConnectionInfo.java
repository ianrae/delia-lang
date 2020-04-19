package org.delia.builder;

import org.delia.db.DBType;

/**
 * Represents a JDBC connection to a database.
 * @author Ian Rae
 *
 */
public class ConnectionInfo {
	public DBType dbType;
	public String jdbcUrl;
	public String userName;
	public String password;
}
