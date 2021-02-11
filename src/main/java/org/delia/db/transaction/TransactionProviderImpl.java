package org.delia.db.transaction;

import org.delia.log.Log;
import org.delia.zdb.DBInterfaceFactory;

public class TransactionProviderImpl implements TransactionProvider {

	private Log log;
	private DBInterfaceFactory dbInterface;

	public TransactionProviderImpl(DBInterfaceFactory dbInterface, Log log) {
		this.dbInterface = dbInterface;
		this.log = log;
	}
	
	@Override
	public void beginTransaction() {
		log.log("beginTransaction..");
	}

	@Override
	public void commitTransaction() {
		log.log("commitTransaction.");
	}

	@Override
	public void rollbackTransaction() {
		log.log("rollbackTransaction.");
	}

}
