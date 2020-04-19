package org.delia.bddnew.core.checker;

import static org.junit.Assert.assertEquals;

import org.delia.bddnew.core.ThenValue;
import org.delia.log.Log;
import org.delia.type.DValue;
import org.delia.type.Shape;

public class IntChecker extends ValueCheckerBase {
	@Override
	public void chkShape(BDDResult bddres) {
		assertEquals(Shape.INTEGER, bddres.res.shape);
	}

	@Override
	public boolean compareObj(ThenValue thenVal, DValue dval, Log log) {
		Integer expected = Integer.parseInt(thenVal.expected);
		Integer s = dval.asInt();
		if (expected == null && s == null) {
			return true;
		} else if (!expected.equals(s)) {
			String err = String.format("value-mismatch: expected '%d' but got '%d'", expected, s);
			log.logError(err);
			return false;
		} else {
			return true;
		}
	}
}