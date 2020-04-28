package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;

public class InsertStatementFragment extends StatementFragmentBase {

		//parallel arrays
		public List<String> setValuesL = new ArrayList<>();
		public InsertStatementFragment assocUpdateFrag; //TODO later support multiple
		public DeleteStatementFragment assocDeleteFrag; //TODO later support multiple
		public MergeIntoStatementFragment assocMergeInfoFrag; //TODO later support multiple
		
		@Override
		public String render() {
			StrCreator sc = new StrCreator();
			sc.o("INSERT");
			sc.o(" %s", tblFrag.render());
			renderInsertFields(sc);
			
			return sc.str;
		}
		
		protected void renderInsertFields(StrCreator sc) {
			if (fieldL.isEmpty()) {
				return;
			}
			
			sc.o(" (");
			ListWalker<FieldFragment> walker = new ListWalker<>(fieldL);
			while(walker.hasNext()) {
				FieldFragment ff = walker.next();
				sc.o(ff.renderAsAliasedFrag());
				walker.addIfNotLast(sc, ", ");
			}
			sc.o(")");

			sc.o(" VALUES(");
			int index = 0;
			walker = new ListWalker<>(fieldL);
			while(walker.hasNext()) {
				walker.next();
				String value = setValuesL.get(index);
				sc.o(value);
				walker.addIfNotLast(sc, ", ");
				index++;
			}
			sc.o(")");
			
			
		}
		
		@Override
		public int getNumSqlParams() {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}