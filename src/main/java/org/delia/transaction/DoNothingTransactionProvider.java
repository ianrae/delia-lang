package org.delia.transaction;

import org.delia.log.DeliaLog;

/**
 * Some databases, such as MEM do not support transactions. 
 * This class implements a do-nothing transaction provider.
 * 
 * @author ian
 *
 */
public class DoNothingTransactionProvider implements TransactionProvider {

	private DeliaLog log;

	public DoNothingTransactionProvider(DeliaLog log) {
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
