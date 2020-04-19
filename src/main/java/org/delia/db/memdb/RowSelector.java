package org.delia.db.memdb;

import java.util.List;

import org.delia.db.QuerySpec;
import org.delia.error.ErrorTracker;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public interface RowSelector {
	void init(ErrorTracker et, QuerySpec spec, DStructType dtype, DTypeRegistry registry);
	List<DValue> match(List<DValue> dval);
	boolean wasError();
	void setTbl(MemDBTable tbl);
	MemDBTable getTbl();
}