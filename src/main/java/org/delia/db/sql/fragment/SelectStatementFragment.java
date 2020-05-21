package org.delia.db.sql.fragment;

import org.delia.db.sql.StrCreator;

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
		return sc.toString();
	}

	@Override
	public int getNumSqlParams() {
		return 0; //not needed
	}

}