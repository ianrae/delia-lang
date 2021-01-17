package org.delia.db.newhls;

import org.apache.commons.lang3.StringUtils;

public class SqlColumn {
	public String alias;
	public String name;
	
	public SqlColumn(String alias, String name) {
		this.alias = alias;
		this.name = name;
	}
	
	public String render() {
		if (StringUtils.isEmpty(alias)) {
			return name;
		}
		return String.format("%s.%s", alias, name);
	}
	public String renderAsTable() {
		if (StringUtils.isEmpty(alias)) {
			return name;
		}
		return String.format("%s as %s", name, alias);
	}
	
	@Override
	public String toString() {
		return String.format("%s.%s", alias, name);
	}
}