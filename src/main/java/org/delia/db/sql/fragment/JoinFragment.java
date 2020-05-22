package org.delia.db.sql.fragment;

import org.delia.db.sql.StrCreator;

public class JoinFragment implements SqlFragment {
	public TableFragment joinTblFrag;
	public FieldFragment arg1;
	public FieldFragment arg2;
	
	@Override
	public String render() {
		StrCreator sc = new StrCreator();
		sc.o(" LEFT JOIN ");
		sc.o(joinTblFrag.render());
		sc.o(" ON ");
		sc.o(arg1.render());
		sc.o("=");
		sc.o(arg2.render());
		return sc.toString();
	}

	@Override
	public int getNumSqlParams() {
		return 0;
	}
}
