package org.delia.bdd.core.checker;

import static org.junit.Assert.assertEquals;

import org.delia.bdd.core.ThenValue;
import org.delia.log.Log;
import org.delia.type.DValue;
import org.delia.type.Shape;

public class LongChecker extends ValueCheckerBase {
	@Override
	public void chkShape(BDDResult bddres) {
		assertEquals(Shape.LONG, bddres.res.shape);
	}

	@Override
	public boolean compareObj(ThenValue thenVal, DValue dval, Log log) {
		Long expected = Long.parseLong(thenVal.expected);
		Long s = dval.asLong();
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