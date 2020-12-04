package org.delia.bdd;

import org.delia.base.DBTestHelper;
import org.delia.zdb.ZDBInterfaceFactory;

public abstract class MemBDDBase extends BDDBase {

	@Override
	public ZDBInterfaceFactory createForTest() {
		ZDBInterfaceFactory db = DBTestHelper.createMEMDb(createFactorySvc());
		return db;
	}


}
