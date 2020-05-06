package org.delia.bdd.core;

import org.delia.db.DBInterface;

public interface DBInterfaceCreator {
	DBInterface createForTest();
}
