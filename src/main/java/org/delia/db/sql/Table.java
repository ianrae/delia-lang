package org.delia.db.sql;

import org.delia.util.StringUtil;

public class Table {
	public String alias;
	public String name;

	public Table() {
	}
	public Table(String alias2, String name) {
		this.alias = alias2;
		this.name = name;
	}

	public String fmtAsStr() {
		return String.format("%s as %s", name, alias);
	}
	
	public String genFmtString(String fieldName) {
		if (StringUtil.isNullOrEmpty(alias)) {
			return fieldName;
		}
		return String.format("%s.%s", alias, fieldName);
	}

	@Override
	public String toString() {
		return genFmtString(name);
	}
	
}