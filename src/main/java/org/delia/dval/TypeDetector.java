package org.delia.dval;

import org.apache.commons.lang3.StringUtils;

public class TypeDetector {

	public static String detectType(String input) {
		if (StringUtils.isEmpty(input)) {
			return "string";
		}

		if (tryBoolean(input)) {
			return "boolean";
		} else if (tryLong(input)) {
			if (tryInt(input)) {
				return "int";
			}
			return "long";
		} else if (tryInt(input)) {
			return "int";
		} else {
			return "string";
		}
	}

	private static boolean tryInt(String input) {
		return isIntValue(input);
	}

	private static boolean tryLong(String input) {
		return isLongValue(input);
	}

	private static boolean tryBoolean(String input) {
		return isBooleanValue(input);
	}
	public static boolean isIntValue(String input) {
		boolean match = false;
		try {
			Integer n = Integer.parseInt(input);
			match = true;
		} catch (NumberFormatException e) {
		}
		return match;
	}

	public static boolean isLongValue(String input) {
		boolean match = false;
		try {
			Long n = Long.parseLong(input);
			match = true;
		} catch (NumberFormatException e) {
		}
		return match;
	}

	public static boolean isBooleanValue(String input) {
		String str = input.toLowerCase();
		if (str.equals("true") || str.equals("false")) {
			return true;
		}
		return false;
	}
}
