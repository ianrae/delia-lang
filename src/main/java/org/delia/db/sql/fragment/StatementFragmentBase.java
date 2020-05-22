package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.ListWalker;

public abstract class StatementFragmentBase implements SqlFragment {
	public SqlStatement statement = new SqlStatement();
	public Map<String,TableFragment> aliasMap = new HashMap<>();
	
	public List<SqlFragment> earlyL = new ArrayList<>();
	public List<FieldFragment> fieldL = new ArrayList<>();
	public TableFragment tblFrag;
	public JoinFragment joinFrag; //FUTURE later a list
	public List<SqlFragment> whereL = new ArrayList<>();
	public LimitFragment limitFrag = null;
	public int paramStartIndex; //used with SqlStatementGroup
	
	public List<FieldFragment> hlsRemapList = new ArrayList<>();
	
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
	
	protected void renderIfPresent(StrCreator sc, SqlFragment frag) {
		if (frag != null) {
			sc.o(frag.render());
		}
	}


	protected void renderEarly(StrCreator sc) {
		for(SqlFragment frag: earlyL) {
			String s = frag.render();
			sc.o(s);
		}
	}

	protected void renderWhereL(StrCreator sc) {
		for(SqlFragment frag: whereL) {
			String s = frag.render();
			sc.o(s);
		}
	}


	protected void renderFields(StrCreator sc) {
		ListWalker<FieldFragment> walker = new ListWalker<>(fieldL);
		while(walker.hasNext()) {
			FieldFragment fieldF = walker.next();
			sc.o(fieldF.render());
			walker.addIfNotLast(sc, ",");
		}
	}

	public void clearFieldList() {
		fieldL.clear();
	}
}