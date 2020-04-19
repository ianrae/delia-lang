package org.delia.db.sql;

public class StrCreator {
	public String str = "";
	
	public String o(String fmt, String...args) {
		String s = String.format(fmt, args);
		str += s;
		return s;
	}
	/**
	 * can't use o() if args contains % chars, so use this method
	 * @param s
	 * @return
	 */
	public String addStr(String s) {
		str += s;
		return s;
	}
	public void nl() {
		str += "\n";
	}
}