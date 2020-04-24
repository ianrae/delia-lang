package org.delia.db.sql.fragment;

import org.apache.commons.lang3.StringUtils;

public class AliasedFragment implements SqlFragment {
	public String alias;
	public String name;
	
	public AliasedFragment() {
	}
	public AliasedFragment(String alias, String name) {
		this.alias = alias;
		this.name = name;
	}
	
	@Override
	public String render() {
		if (StringUtils.isEmpty(alias)) {
			return name;
		}
		return String.format("%s.%s", alias, name);
	}
}