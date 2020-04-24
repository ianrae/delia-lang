package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.ListWalker;

public class SelectStatementFragment implements SqlFragment {
	public SqlStatement statement = new SqlStatement();
	public Map<String,TableFragment> aliasMap = new HashMap<>();
	
	public List<SqlFragment> earlyL = new ArrayList<>();
	public List<FieldFragment> fieldL = new ArrayList<>();
	public TableFragment tblFrag;
	public JoinFragment joinFrag; //TODO later a list
	public List<SqlFragment> whereL = new ArrayList<>();
	public OrderByFragment orderByFrag = null;
	public LimitFragment limitFrag = null;
	public OffsetFragment offsetFrag = null;
	
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
	
	@Override
	public String render() {
		StrCreator sc = new StrCreator();
		sc.o("SELECT ");
		renderEarly(sc);
		renderFields(sc);
		sc.o(" FROM %s", tblFrag.render());
		renderIfPresent(sc, joinFrag);
		
		if (! whereL.isEmpty()) {
			sc.o(" WHERE ");
			renderWhereL(sc);
		}
		
		renderIfPresent(sc, orderByFrag);
		renderIfPresent(sc, limitFrag);
		renderIfPresent(sc, offsetFrag);
		return sc.str;
	}


	private void renderIfPresent(StrCreator sc, SqlFragment frag) {
		if (frag != null) {
			sc.o(frag.render());
		}
	}


	private void renderEarly(StrCreator sc) {
		for(SqlFragment frag: earlyL) {
			String s = frag.render();
			sc.o(s);
		}
	}

	private void renderWhereL(StrCreator sc) {
		for(SqlFragment frag: whereL) {
			String s = frag.render();
			sc.o(s);
		}
	}


	private void renderFields(StrCreator sc) {
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