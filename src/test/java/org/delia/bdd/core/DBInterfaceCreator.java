package org.delia.bdd.core;

import org.delia.zdb.ZDBInterfaceFactory;

public interface DBInterfaceCreator {
	ZDBInterfaceFactory createForTest();
}
