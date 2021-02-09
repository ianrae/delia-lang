package org.delia;

import javax.sql.DataSource;

import org.delia.builder.ConnectionInfo;
import org.delia.db.DBType;
import org.delia.db.sql.ConnectionString;

/**
 * Builds a ConnectionDefinition object which delia uses to connect to the db.
 * @author ian
 *
 */
public class ConnectionStringBuilder {

	public static ConnectionString create(ConnectionInfo info) {
		ConnectionString connStr = new ConnectionString(info.dbType, info.userName, info.password, info.jdbcUrl);
		return connStr;
	}
	public static ConnectionString create(DBType dbType, DataSource ds, String userName, String password) {
		ConnectionString connStr = new ConnectionString(dbType, userName, password, ds);
		return connStr;
	}
	public static ConnectionString createMEM() {
		ConnectionString connStr = new ConnectionString();
		connStr.dbType = DBType.MEM;
		//other fields not needed for MEM
		return connStr;
	}
	
}
