package org.delia.bdd.core.checker;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.delia.bdd.core.BDDHelper;
import org.delia.bdd.core.ThenValue;
import org.delia.compiler.generate.DeliaGeneratePhase;
import org.delia.compiler.generate.SimpleFormatOutputGenerator;
import org.delia.log.Log;
import org.delia.type.DValue;

public class StructChecker extends ValueCheckerBase {
	@Override
	public void chkShape(BDDResult bddres) {
		// assertEquals(Shape.STRUCT, bddres.res.shape);
		// res.shape is null when query
	}

	@Override
	public boolean compareObj(ThenValue thenVal, DValue dval, Log log) {
		if (thenVal.isEmpty()) {
			String err = String.format("value-mismatch: struct must use then: clause");
			log.logError(err);
			return false;
		}

		List<String> genL = generateFromDVal(dval);
		List<String> thenL = generateFromThen(thenVal);

		return compareStringLists(genL, thenL, log);
	}
	public boolean compareMultiObj(ThenValue thenVal, List<DValue> list, Log log) {
		if (thenVal.isEmpty()) {
			String err = String.format("value-mismatch: struct must use then: clause");
			log.logError(err);
			return false;
		}

		List<String> genL = generateFromMultiDVal(list);
		List<String> thenL = generateFromThen(thenVal);

		boolean pass = compareStringLists(genL, thenL, log);
		return pass;
	}
	
	private boolean compareStringLists(List<String> genL, List<String> thenL, Log log) {
		thenL = adjustForDBType(thenL);
		int index = 0;
		for (String actualLine : genL) {
			if (index >= thenL.size()) {
				String err = String.format("'then:' text is too short: '%s'", actualLine);
				log.logError(err);
				boolean once = true;
				for(String x: genL) {
					if (once) {
						log.log("actual: %s", x);
						once = false;
					} else {
						log.log("%s", x);
					}
				}
				return false;
			}
			String s = thenL.get(index);
			s = s.trim();
			actualLine = actualLine.trim();
			if (!s.equals(actualLine)) {
				String err = String.format("value-mismatch: (line %d) expected '%s' but got '%s'", index, s, actualLine);
				log.logError(err);
				boolean once = true;
				int k = 0;
				for(String x: genL) {
					if (once) {
						log.log("actual%d: %s", k, x);
						once = false;
					} else {
						log.log("%d: %s", k, x);
					}
					k++;
				}
				return false;
			}
			index++;
		}
		
		if (genL.size() != thenL.size()) {
			String err = String.format("value-mismatch: resultList has %d but thenL has %d", genL.size(), thenL.size());
			log.logError(err);
			return false;
		}		
		return true;
	}

	/**
	 * Some DBTypes return a bit extra information. This is not wrong data, just a bit more than you asked for.
	 * So in a bdd "then:" clause you can specify dbtype-specific text
	 *  IF(MEM):vworker:{55}
	 *  ELSE:  vworker:{55:
	 *  ELSE:  {
	 * etc.
	 * @param thenL
	 * @return
	 */
	private List<String> adjustForDBType(List<String> thenL) {
		BDDHelper helper = new BDDHelper(this.sess.getDelia().getDBInterface().getDBType());
		return helper.adjustForDBType(thenL);
	}

	private List<String> generateFromThen(ThenValue thenVal) {
		if (thenVal.expected != null) {
			return Collections.singletonList(thenVal.expected);
		}
		return thenVal.expectedL;
	}

	private List<String> generateFromDVal(DValue dval) {
		SimpleFormatOutputGenerator gen = new SimpleFormatOutputGenerator();
		gen.truncateLargeBlob = true;
		// ErrorTracker et = new SimpleErrorTracker(log);
		DeliaGeneratePhase phase = this.sess.getExecutionContext().generator;
		boolean b = phase.generateValue(gen, dval, "a");
		assertEquals(true, b);
		return gen.outputL;
	}
	private List<String> generateFromMultiDVal(List<DValue> list) {
		SimpleFormatOutputGenerator gen = new SimpleFormatOutputGenerator();
		gen.truncateLargeBlob = true;
		// ErrorTracker et = new SimpleErrorTracker(log);
		DeliaGeneratePhase phase = this.sess.getExecutionContext().generator;
		for(DValue dval: list) {
			boolean b = phase.generateValue(gen, dval, "a");
			assertEquals(true, b);
		}
		return gen.outputL;
	}

}