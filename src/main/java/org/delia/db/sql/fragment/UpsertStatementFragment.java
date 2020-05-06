package org.delia.db.sql.fragment;


import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;

public class UpsertStatementFragment extends UpdateStatementFragment {

	public String sqlCmd = "MERGE INTO";
	public RawFragment keyFrag;
	public String keyFieldName;
	public boolean addOnConflictPhrase; //for postgres
	public boolean noUpdateFlag;

	@Override
	public String render() {
		StrCreator sc = new StrCreator();
		sc.o(sqlCmd);
		sc.o(" %s", tblFrag.render());

		renderColumnNames(sc);

		if (keyFrag != null) {
			sc.o(keyFrag.render());
		}
		renderUpdateFields(sc);

		if (addOnConflictPhrase) {
			addOnConflictPhrase(sc);
		}

		return sc.str;
	}

	//		sc.o(" ON CONFLICT (leftv,rightv) DO UPDATE SET leftv = ?,rightv=?");
	private void addOnConflictPhrase(StrCreator sc) {
		if (noUpdateFlag) {
			sc.o(" ON CONFLICT (%s) DO NOTHING", keyFieldName);
			return;
		}
		
		sc.o(" ON CONFLICT (%s) DO UPDATE", keyFieldName);
		sc.o(" SET ");
		int index = 0;
		ListWalker<FieldFragment> walker = new ListWalker<>(fieldL);
		while(walker.hasNext()) {
			FieldFragment ff = walker.next();
			String value = setValuesL.get(index);
			sc.o("%s = %s", renderSetField(ff), value);
			walker.addIfNotLast(sc, ", ");
			index++;
		}
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