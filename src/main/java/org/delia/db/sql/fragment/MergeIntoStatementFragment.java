package org.delia.db.sql.fragment;

import org.delia.db.sql.StrCreator;

public class MergeIntoStatementFragment extends StatementFragmentBase {
	public String prefix = "MERGE INTO";
	public String afterEarlyPrefix = "";
	public RawFragment rawFrag;
	
	@Override
	public String render() {
		StrCreator sc = new StrCreator();
		sc.o(prefix);
		renderEarly(sc);
		sc.o(afterEarlyPrefix);
		sc.o(" %s", tblFrag.render());
//		renderIfPresent(sc, joinFrag);
		
//		if (! whereL.isEmpty()) {
//			sc.o(" WHERE ");
//			renderWhereL(sc);
//		}
		
		sc.o(rawFrag.render());
		
//		renderIfPresent(sc, limitFrag);
		return sc.toString();
	}
	
	@Override
	public int getNumSqlParams() {
		return 0;//not needed
	}
}