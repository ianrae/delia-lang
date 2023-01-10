package org.delia.util;

public class StrCreator {
	private StringBuilder sb = new StringBuilder();

	public String o(String fmt, String...args) {
		String s = String.format(fmt, args);
		sb.append(s);
		return s;
	}
	/**
	 * can't use o() if args contains % chars, so use this method
	 * @param s string to output
	 * @return the input string s
	 */
	public String addStr(String s) {
		sb.append(s);
		return s;
	}
	public void nl() {
		sb.append('\n');
	}
	@Override
	public String toString() {
		return sb.toString();
	}
}