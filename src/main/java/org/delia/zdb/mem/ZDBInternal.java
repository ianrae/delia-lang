package org.delia.zdb.mem;

import org.delia.db.memdb.MemDBTable;
import org.delia.runner.FetchRunner;

public interface ZDBInternal {
	FetchRunner doCreateFetchRunner();
	MemDBTable handleUnknownTable(String typeName);
}
