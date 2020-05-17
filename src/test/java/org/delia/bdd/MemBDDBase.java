package org.delia.bdd;

import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;

public abstract class MemBDDBase extends NewBDDBase {

	@Override
	public ZDBInterfaceFactory createForTest() {
		MemZDBInterfaceFactory db = new MemZDBInterfaceFactory(createFactorySvc());
		return db;
	}


}
