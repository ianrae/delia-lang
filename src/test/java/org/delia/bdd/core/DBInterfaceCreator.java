package org.delia.bdd.core;

import org.delia.db.DBInterface;
import org.delia.zdb.ZDBInterfaceFactory;

public interface DBInterfaceCreator {
	ZDBInterfaceFactory createForTest();
}
