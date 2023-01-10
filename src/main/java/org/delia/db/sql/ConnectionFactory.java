package org.delia.db.sql;

import org.delia.db.DBErrorConverter;

import java.sql.Connection;

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
	ConnectionDefinition getConnectionString();
	void setErrorConverter(DBErrorConverter errorConverter);
	String getConnectionSummary();
}
