package org.delia.transaction;

import org.delia.db.DBConnectionInternal;
import org.delia.db.DBInterfaceFactory;
import org.delia.log.DeliaLog;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionProviderImpl implements TransactionProvider, TransactionAdapter {

	private DeliaLog log;
	private DBInterfaceFactory dbInterface;
	private TransactionAwareDBInterface transAwareDBInterface;
//	private DBConnection dbConn;
	private TransactionAwareDBConnection taConn;


	public TransactionProviderImpl(DBInterfaceFactory dbInterface, DeliaLog log) {
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
		this.taConn = transAwareDBInterface.getCurrentConn();
		Connection jdbcConn = getJdbcConn(); 
		try {
			jdbcConn.setAutoCommit(false);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void commitTransaction() {
		log.log("commitTransaction.");
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
