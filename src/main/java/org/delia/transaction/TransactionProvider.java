package org.delia.transaction;

public interface TransactionProvider {
	void beginTransaction();
	void commitTransaction();
	void rollbackTransaction();
}
