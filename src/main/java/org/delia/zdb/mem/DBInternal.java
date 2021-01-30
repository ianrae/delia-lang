package org.delia.zdb.mem;

import org.delia.db.memdb.MemDBTable;
import org.delia.runner.FetchRunner;

public interface DBInternal {
	FetchRunner doCreateFetchRunner();
	MemDBTable handleUnknownTable(String typeName);
}
