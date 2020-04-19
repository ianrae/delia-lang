package org.delia.db.sql;

import java.sql.Connection;

import org.delia.db.DBErrorConverter;

/**
 * Factory that creates jdbc connections.
 * Write your own class that implements this interface to
 * do custom connection creation.
 * 
 * @author Ian Rae
 *
 */
public interface ConnectionFactory {

	Connection createConnection();
	ConnectionString getConnectionString();
	void setErrorConverter(DBErrorConverter errorConverter);
	String getConnectionSummary();
}
