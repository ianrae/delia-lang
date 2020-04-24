package org.delia.db.sql.fragment;

import org.apache.commons.lang3.StringUtils;

public class TableFragment extends AliasedFragment {
	@Override
	public String render() {
		if (StringUtils.isEmpty(alias)) {
			return name;
		}
		return String.format("%s as %s", name, alias);
	}
}