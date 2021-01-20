package org.delia.db.sqlgen;

import org.delia.db.newhls.cud.HLDDelete;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;

public class SqlDeleteStatement implements SqlStatementGenerator {

	private SqlTableNameClause tblClause;
	private SqlWhereClause whereClause;
	
	private HLDDelete hld;

	public SqlDeleteStatement(SqlTableNameClause tblClause, SqlWhereClause whereClause) {
		this.tblClause = tblClause;
		this.whereClause = whereClause;
	}

	public void init(HLDDelete hld) {
		this.hld = hld;
		tblClause.init(hld.typeOrTbl);
		whereClause.init(hld.hld);
	}
	
	@Override
	public SqlStatement render() {
		SqlStatement stm = new SqlStatement(hld);
		StrCreator sc = new StrCreator();
		sc.o("DELETE FROM");
		sc.o(tblClause.render(stm));

		sc.o(whereClause.render(stm));
		stm.sql = sc.toString();
		return stm;
	}

}
