package org.delia.db.sql.fragment;

import org.apache.commons.lang3.StringUtils;
import org.delia.type.DStructType;

public class TableFragment extends AliasedFragment {
	public DStructType structType; //can be null for assoc table
	
	public TableFragment() {
	}
	public TableFragment(TableFragment old) {
		this.alias = old.alias;
		this.name = old.name;
		this.structType = old.structType;
	}
	
	@Override
	public String render() {
		if (StringUtils.isEmpty(alias)) {
			return name;
		}
		return String.format("%s as %s", name, alias);
	}
	
}