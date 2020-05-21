package org.delia.db.sql.fragment;

import org.delia.db.sql.StrCreator;

public class DeleteStatementFragment extends StatementFragmentBase {
	
	@Override
	public String render() {
		StrCreator sc = new StrCreator();
		sc.o("DELETE");
		renderEarly(sc);
		sc.o(" FROM %s", tblFrag.render());
		renderIfPresent(sc, joinFrag);
		
		if (! whereL.isEmpty()) {
			sc.o(" WHERE ");
			renderWhereL(sc);
		}
		
		renderIfPresent(sc, limitFrag);
		return sc.toString();
	}
	
	@Override
	public int getNumSqlParams() {
		return 0;//not needed
	}
}