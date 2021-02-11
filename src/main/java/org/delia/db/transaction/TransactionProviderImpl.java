package org.delia.db.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import org.delia.db.DBType;
import org.delia.log.Log;
import org.delia.zdb.DBConnectionInternal;
import org.delia.zdb.DBInterfaceFactory;

public class TransactionProviderImpl implements TransactionProvider, TransactionAdapter {

	private Log log;
	private DBInterfaceFactory dbInterface;
	private TransactionAwareDBInterface transAwareDBInterface;
//	private DBConnection dbConn;
	private TransactionAwareDBConnection taConn;


	public TransactionProviderImpl(DBInterfaceFactory dbInterface, Log log) {
		this.dbInterface = dbInterface;
		this.log = log;
	}
	
	@Override
	public void beginTransaction() {
		log.log("beginTransaction..");
		if (transAwareDBInterface == null) {
			transAwareDBInterface = new TransactionAwareDBInterface(dbInterface);
		}
		transAwareDBInterface.openConnection();
		if (isMEMDb()) {
			return; //nothing to do
		}
		this.taConn = transAwareDBInterface.getCurrentConn();
		Connection jdbcConn = getJdbcConn(); 
		try {
			jdbcConn.setAutoCommit(false);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isMEMDb() {
		return dbInterface.getDBType().equals(DBType.MEM);
	}

	@Override
	public void commitTransaction() {
		log.log("commitTransaction.");
		if (isMEMDb()) {
			return; //nothing to do
		}
		Connection jdbcConn = getJdbcConn(); 
		try {
			jdbcConn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			taConn.actuallyClose();
		}
	}

	private Connection getJdbcConn() {
		TransactionAwareDBConnection conn = transAwareDBInterface.getCurrentConn();
		DBConnectionInternal conni = conn.getConnInternal();
		return conni.getJdbcConnection();
	}

	@Override
	public void rollbackTransaction() {
		log.log("rollbackTransaction.");
		if (isMEMDb()) {
			return; //nothing to do
		}
		Connection jdbcConn = getJdbcConn(); 
		try {
			jdbcConn.rollback();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			taConn.actuallyClose();
		}
	}

	@Override
	public TransactionAwareDBInterface getTransactionAwareDBInterface() {
		if (transAwareDBInterface == null) {
			transAwareDBInterface = new TransactionAwareDBInterface(dbInterface);
		}
		return transAwareDBInterface;
	}
}
