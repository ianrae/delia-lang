package org.delia.db.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.delia.db.DBErrorConverter;
import org.delia.log.Log;

/**
 * A sample connection factory.  
 * @author Ian Rae
 *
 */
public class ConnectionFactoryImpl implements ConnectionFactory {
	
	public ConnectionString connectionString;
	private DBErrorConverter errorConverter;
	private Log log;
	private boolean haveLoggedJDBC = false;
	
	public ConnectionFactoryImpl(ConnectionString connStr, Log log) {
		this.connectionString = connStr;
		this.log = log;
	}

	@Override
	public Connection createConnection() {
		Connection conn = null;
		try {
			if (! haveLoggedJDBC) {
				haveLoggedJDBC = true;
				log.log("JDBC: url: %s", connectionString.jdbcUrl);
				log.log("JDBC: user: %s, pwd: %s", connectionString.userName, connectionString.pwd);
			}
			conn = DriverManager.getConnection(connectionString.jdbcUrl, connectionString.userName, connectionString.pwd);
		} catch (SQLException e) {
			errorConverter.convertAndRethrowException(e);
		}      
		return conn;
	}

	@Override
	public ConnectionString getConnectionString() {
		return this.connectionString;
	}

	@Override
	public void setErrorConverter(DBErrorConverter errorConverter) {
		this.errorConverter = errorConverter;
	}

	@Override
	public String getConnectionSummary() {
		return connectionString.jdbcUrl;
	}

}
