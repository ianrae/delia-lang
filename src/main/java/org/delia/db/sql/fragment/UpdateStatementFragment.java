package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;

public class UpdateStatementFragment extends SelectStatementFragment {

		//parallel arrays
		public List<String> setValuesL = new ArrayList<>();
		public UpdateStatementFragment assocUpdateFrag; //TODO later support multiple
		
		@Override
		public String render() {
			StrCreator sc = new StrCreator();
			sc.o("UPDATE");
			//renderEarly(sc);
			sc.o(" %s", tblFrag.render());
			renderUpdateFields(sc);
//			renderIfPresent(sc, joinFrag);

			if (! whereL.isEmpty()) {
				sc.o(" WHERE");
				renderWhereL(sc);
			}

//			renderIfPresent(sc, orderByFrag);
			renderIfPresent(sc, limitFrag);
			
			if (this.assocUpdateFrag != null) {
				sc.o(";\n");
				String ss = this.assocUpdateFrag.render();
				sc.o(ss);
			}
			return sc.str;
		}
		
		protected void renderUpdateFields(StrCreator sc) {
			if (fieldL.isEmpty()) {
				return;
			}
			
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
		
		private String renderSetField(FieldFragment fieldF) {
			String suffix = fieldF.asName == null ? "" : " as " + fieldF.asName;
			return String.format("%s%s", fieldF.renderAsAliasedFrag(), suffix);
		}
		
	}