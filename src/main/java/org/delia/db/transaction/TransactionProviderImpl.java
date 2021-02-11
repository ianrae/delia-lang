package org.delia.db.transaction;

import org.delia.log.Log;
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
		TransactionAwareDBConnection conn = transAwareDBInterface.getCurrentConn();
		//TODO conn.commit()
	}

	@Override
	public void rollbackTransaction() {
		log.log("rollbackTransaction.");
		TransactionAwareDBConnection conn = transAwareDBInterface.getCurrentConn();
		//TODO conn.rollback
	}

	@Override
	public DBInterfaceFactory createTransactionAwareDBInterface() {
		this.transAwareDBInterface = new TransactionAwareDBInterface(dbInterface);
		return transAwareDBInterface;
	}

}
