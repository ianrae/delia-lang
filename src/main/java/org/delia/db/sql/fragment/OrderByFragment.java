package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;

public class OrderByFragment extends AliasedFragment {
	public String asc;
	public List<OrderByFragment> additionalL = new ArrayList<>();

	@Override
	public String render() {
		StrCreator sc = new StrCreator();
		String s = asc == null ? "" : " " + asc;
		sc.o(" ORDER BY %s%s", super.render(), s);
		if (!additionalL.isEmpty()) {
			sc.o(", ");
			ListWalker<OrderByFragment> walker = new ListWalker<>(additionalL);
			while(walker.hasNext()) {
				OrderByFragment frag = walker.next();
				sc.o(renderPhrase(frag));
				walker.addIfNotLast(sc, ",");
			}
		}
		
		return sc.toString();
	}
	
	private String renderPhrase(OrderByFragment frag) {
		String s = frag.asc == null ? "" : " " + frag.asc;
		AliasedFragment afrag = new AliasedFragment(frag.alias, frag.name);
		return String.format("%s%s", afrag.render(), s);
	}
	
	@Override
	public int getNumSqlParams() {
		return 0;
	}
}