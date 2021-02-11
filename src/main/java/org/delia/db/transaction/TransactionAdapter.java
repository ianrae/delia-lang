package org.delia.db.transaction;

import org.delia.zdb.DBInterfaceFactory;

public interface TransactionAdapter {
	DBInterfaceFactory createTransactionAwareDBInterface();
}
