package org.delia.db.sqlgen;

import org.delia.db.hld.cud.TypeOrTable;
import org.delia.db.sql.prepared.SqlStatement;

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
