package org.delia.db.sqlgen;

import org.delia.db.SqlStatement;
import org.delia.hld.cud.TypeOrTable;

public class SqlTableNameClause implements SqlClauseGenerator {
	
	private TypeOrTable typeOrTbl;

	public void init(TypeOrTable typeOrTbl) {
		this.typeOrTbl = typeOrTbl;
	}
	@Override
	public String render(SqlStatement stm) {
		return typeOrTbl.render();
	}

}
