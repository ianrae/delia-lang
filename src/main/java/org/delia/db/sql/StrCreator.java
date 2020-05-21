package org.delia.db.sql;

public class StrCreator {
	private String str = ""; //TODO use StringBuilder for perf
	
	public String o(String fmt, String...args) {
		String s = String.format(fmt, args);
		str += s;
		return s;
	}
	/**
	 * can't use o() if args contains % chars, so use this method
	 * @param s string to output
	 * @return the input string s
	 */
	public String addStr(String s) {
		str += s;
		return s;
	}
	public void nl() {
		str += "\n";
	}
	@Override
	public String toString() {
		return str;
	}
}