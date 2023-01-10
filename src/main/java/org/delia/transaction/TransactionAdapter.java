package org.delia.transaction;


public interface TransactionAdapter {
//	DBInterfaceFactory createTransactionAwareDBInterface();
	TransactionAwareDBInterface getTransactionAwareDBInterface();
}
