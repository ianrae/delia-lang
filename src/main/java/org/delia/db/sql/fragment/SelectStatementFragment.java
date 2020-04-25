package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;

public class SelectStatementFragment extends StatementFragmentBase {
	
	public OrderByFragment orderByFrag = null;
	public OffsetFragment offsetFrag = null;
	
	
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