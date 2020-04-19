package org.delia.bddnew.core.checker;

import org.delia.api.DeliaSession;

public abstract class ValueCheckerBase implements ValueChecker {
	protected DeliaSession sess;

	@Override
	public void setDBSession(DeliaSession sess) {
		this.sess = sess;
	}
}