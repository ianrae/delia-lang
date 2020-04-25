package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.db.sql.prepared.SqlStatement;

public abstract class StatementFragmentBase implements SqlFragment {
	public SqlStatement statement = new SqlStatement();
	public Map<String,TableFragment> aliasMap = new HashMap<>();
	
	public List<SqlFragment> earlyL = new ArrayList<>();
	public List<FieldFragment> fieldL = new ArrayList<>();
	public TableFragment tblFrag;
	public JoinFragment joinFrag; //TODO later a list
	public List<SqlFragment> whereL = new ArrayList<>();
	public LimitFragment limitFrag = null;
	
	
	public TableFragment findByAlias(String alias) {
		if (alias == null) {
			return null;
		}
		for(String s: aliasMap.keySet()) {
			TableFragment frag = aliasMap.get(s);
			if (frag.alias.equals(alias)) {
				return frag;
			}
		}
		return null;
	}
	public TableFragment findByTableName(String tblName) {
		if (tblName == null) {
			return null;
		}
		for(String s: aliasMap.keySet()) {
			TableFragment frag = aliasMap.get(s);
			if (frag.name.equals(tblName)) {
				return frag;
			}
		}
		return null;
	}
	
}