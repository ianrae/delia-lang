package org.delia.db.hls;

import org.delia.db.sql.StrCreator;

public class SQLCreator {
	private StrCreator sc = new StrCreator();
	private boolean isPrevious = false;

	public String out(String fmt, String...args) {
		if (isPrevious) {
			sc.o(" ");
		}
		String s = sc.o(fmt, args);
		isPrevious = true;
		return s;
	}

	public String sql() {
		return sc.str;
	}
}