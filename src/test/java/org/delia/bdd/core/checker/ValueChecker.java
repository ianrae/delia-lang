package org.delia.bdd.core.checker;

import org.delia.DeliaSession;
import org.delia.bdd.core.ThenValue;
import org.delia.log.Log;
import org.delia.type.DValue;

public interface ValueChecker {
	void chkShape(BDDResult bddres);

	void setDBSession(DeliaSession sess);

	boolean compareObj(ThenValue thenVal, DValue dval, Log log);
}