package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;

public class UpsertStatementFragment extends SelectStatementFragment {

	public String sqlCmd = "MERGE INTO";
	public List<String> setValuesL = new ArrayList<>();
	//		public boolean doUpdateLast = false;
	public RawFragment keyFrag;
	public String keyFieldName;

	@Override
	public String render() {
		StrCreator sc = new StrCreator();
		sc.o(sqlCmd);
		//renderEarly(sc);
		sc.o(" %s", tblFrag.render());

		renderColumnNames(sc);

		if (keyFrag != null) {
			sc.o(keyFrag.render());
		}
		renderUpdateFields(sc);

		renderIfPresent(sc, limitFrag);

		return sc.str;
	}

	private void renderColumnNames(StrCreator sc) {
		sc.o(" (");
		ListWalker<FieldFragment> walker = new ListWalker<>(fieldL);
		int index = 0;
		while(walker.hasNext()) {
			if (index == 0) {
				sc.o(keyFieldName);
				if (! fieldL.isEmpty()) {
					sc.o(",");
				}
			}
			FieldFragment ff = walker.next();
			String value = ff.name;
			sc.o(value);
			walker.addIfNotLast(sc, ", ");
			index++;
		}
		sc.o(")");

	}

	protected void renderUpdateFields(StrCreator sc) {
		if (fieldL.isEmpty()) {
			return;
		}

		sc.o(" VALUES(");
		int index = 0;
		ListWalker<String> walker = new ListWalker<>(setValuesL);
		while(walker.hasNext()) {
			if (index == 0) {
				sc.o("?");
				if (setValuesL.size() > 0) {
					sc.o(",");
				}
			}
			walker.next();
			String value = setValuesL.get(index);
			sc.o(value);
			walker.addIfNotLast(sc, ", ");
			index++;
		}
		sc.o(")");
	}

	private String renderSetField(FieldFragment fieldF) {
		String suffix = fieldF.asName == null ? "" : " as " + fieldF.asName;
		return String.format("%s%s", fieldF.renderAsAliasedFrag(), suffix);
	}

}