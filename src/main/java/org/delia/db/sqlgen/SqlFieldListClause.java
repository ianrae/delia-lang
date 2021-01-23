package org.delia.db.sqlgen;

import java.util.List;

import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.ListWalker;
import org.delia.hld.HLDField;

public class SqlFieldListClause implements SqlClauseGenerator {

	private List<HLDField> fieldL;
	
	public void init(List<HLDField> fieldL) {
		this.fieldL = fieldL;
	}
	@Override
	public String render(SqlStatement stm) {
		StrCreator sc = new StrCreator();
		if (fieldL.isEmpty()) {
			sc.o(" DEFAULT VALUES");
			return sc.toString();
		}
		
		sc.o(" (");
		ListWalker<HLDField> walker = new ListWalker<>(fieldL);
		while(walker.hasNext()) {
			HLDField ff = walker.next();
			ff.alias = null;
			sc.o(ff.render());
			walker.addIfNotLast(sc, ", ");
		}
		sc.o(")");
		return sc.toString();
	}

}
