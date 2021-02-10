package org.delia.db.transaction;

public interface TransactionProvider {
	void beginTransaction();
	void commitTransaction();
	void rollbackTransaction();
}
