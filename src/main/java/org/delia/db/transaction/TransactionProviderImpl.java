package org.delia.db.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import org.delia.log.Log;
import org.delia.zdb.DBConnectionInternal;
import org.delia.zdb.DBInterfaceFactory;

public class TransactionProviderImpl implements TransactionProvider, TransactionAdapter {

	private Log log;
	private DBInterfaceFactory dbInterface;
	private TransactionAwareDBInterface transAwareDBInterface;

	public TransactionProviderImpl(DBInterfaceFactory dbInterface, Log log) {
		this.dbInterface = dbInterface;
		this.log = log;
	}
	
	@Override
	public void beginTransaction() {
		log.log("beginTransaction..");
		//transAwareDBInterface will lazily start the transaction
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
		}
	}

	@Override
	public DBInterfaceFactory createTransactionAwareDBInterface() {
		this.transAwareDBInterface = new TransactionAwareDBInterface(dbInterface);
		return transAwareDBInterface;
	}

}
