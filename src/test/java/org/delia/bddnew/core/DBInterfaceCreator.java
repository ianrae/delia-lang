package org.delia.bddnew.core;

import org.delia.db.DBInterface;

public interface DBInterfaceCreator {
	DBInterface createForTest();
}
