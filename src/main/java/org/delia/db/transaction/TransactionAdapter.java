package org.delia.db.transaction;


public interface TransactionAdapter {
//	DBInterfaceFactory createTransactionAwareDBInterface();
	TransactionAwareDBInterface getTransactionAwareDBInterface();
}
