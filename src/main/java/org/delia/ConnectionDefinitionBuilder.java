package org.delia;

import org.delia.db.DBType;
import org.delia.db.sql.ConnectionDefinition;

import javax.sql.DataSource;

/**
 * Builds a ConnectionDefinition object which delia uses to connect to the db.
 * @author ian
 *
 */
public class ConnectionDefinitionBuilder {

//	public static ConnectionDefinition create(ConnectionInfo info) {
//		ConnectionDefinition connStr = new ConnectionDefinition(info.dbType, info.userName, info.password, info.jdbcUrl);
//		return connStr;
//	}
	public static ConnectionDefinition create(DBType dbType, DataSource ds, String userName, String password) {
		ConnectionDefinition connStr = new ConnectionDefinition(dbType, userName, password, ds);
		return connStr;
	}
	public static ConnectionDefinition createMEM() {
		ConnectionDefinition connStr = new ConnectionDefinition();
		connStr.dbType = DBType.MEM;
		//other fields not needed for MEM
		return connStr;
	}
	
}
