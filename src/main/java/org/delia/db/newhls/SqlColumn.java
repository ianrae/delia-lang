package org.delia.db.newhls;

class SqlColumn {
	public String alias;
	public String name;
	
	public SqlColumn(String alias, String name) {
		this.alias = alias;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return String.format("%s.%s", alias, name);
	}
}