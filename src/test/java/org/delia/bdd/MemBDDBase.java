package org.delia.bdd;

import org.delia.base.DBTestHelper;
import org.delia.zdb.DBInterfaceFactory;

public abstract class MemBDDBase extends BDDBase {

	@Override
	public DBInterfaceFactory createForTest() {
		DBInterfaceFactory db = DBTestHelper.createMEMDb(createFactorySvc());
		return db;
	}


}
