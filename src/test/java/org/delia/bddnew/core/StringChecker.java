//package org.delia.bddnew.core;
//
//import org.delia.bdd.core.ThenValue;
//import org.delia.log.Log;
//import org.delia.type.DValue;
//import org.delia.type.Shape;
//
//public class StringChecker extends ValueCheckerBase {
//	@Override
//	public void chkShape(BDDResult bddres) {
//		assertEquals(Shape.STRING, bddres.res.shape);
//	}
//
//	@Override
//	public boolean compareObj(ThenValue thenVal, DValue dval, Log log) {
//		String expected = thenVal.expected;
//		String s = dval.asString();
//		expected = removeQuotesIfNeeded(expected);
//		expected = handleEscapeChars(expected);
//		if (expected == null && s == null) {
//			return true;
//		} else if (!expected.equals(s)) {
//			String err = String.format("value-mismatch: expected '%s' but got '%s'", expected, s);
//			log.logError(err);
//			return false;
//		} else {
//			return true;
//		}
//	}
//
//	private String handleEscapeChars(String s) {
//		// if (s.indexOf('\n') >= 0) {
//		if (s.contains("\\n")) {
//			s = s.replace("\\n", "\n");
//		}
//
//		if (s.contains("\\r")) {
//			s = s.replace("\\r", "\r");
//		}
//
//		if (s.contains("\\t")) {
//			s = s.replace("\\t", "\t");
//		}
//		return s;
//	}
//
//	private String removeQuotesIfNeeded(String expected) {
//		expected = expected.trim();
//		if (expected.length() == 2) {
//			return "";
//		}
//
//		if (expected.startsWith("'") && expected.endsWith("'")) {
//			return expected.substring(1, expected.length() - 1);
//		} else if (expected.startsWith("\"") && expected.endsWith("\"")) {
//			return expected.substring(1, expected.length() - 1);
//		}
//		return expected;
//	}
//}