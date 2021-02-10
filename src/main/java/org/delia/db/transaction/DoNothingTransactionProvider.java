package org.delia.db.transaction;

import org.delia.log.Log;

public class DoNothingTransactionProvider implements TransactionProvider {

	private Log log;

	public DoNothingTransactionProvider(Log log) {
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
