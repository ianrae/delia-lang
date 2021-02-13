package org.delia.bdd.core.checker;

import org.delia.DeliaSession;

public abstract class ValueCheckerBase implements ValueChecker {
	protected DeliaSession sess;

	@Override
	public void setDBSession(DeliaSession sess) {
		this.sess = sess;
	}
}