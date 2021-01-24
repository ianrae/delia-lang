package org.delia.bdd.core;

import org.delia.zdb.DBInterfaceFactory;

public interface DBInterfaceCreator {
	DBInterfaceFactory createForTest();
}
