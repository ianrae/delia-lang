package org.delia.bddnew.core.checker;

import org.delia.api.DeliaSession;
import org.delia.bddnew.core.ThenValue;
import org.delia.log.Log;
import org.delia.type.DValue;

public interface ValueChecker {
	void chkShape(BDDResult bddres);

	void setDBSession(DeliaSession sess);

	boolean compareObj(ThenValue thenVal, DValue dval, Log log);
}