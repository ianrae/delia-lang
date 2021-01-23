package org.delia.db.sqlgen;

import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.hld.cud.HLDDelete;

public class SqlDeleteStatement implements SqlStatementGenerator {

	private SqlTableNameClause tblClause;
	private SqlWhereClause whereClause;
	
	private HLDDelete hld;
	private SqlDeleteInClause deleteInClause;

	public SqlDeleteStatement(SqlTableNameClause tblClause, SqlWhereClause whereClause) {
		this.tblClause = tblClause;
		this.whereClause = whereClause;
	}

	public void useDeleteIn(SqlDeleteInClause deleteInClause) {
		this.deleteInClause = deleteInClause;
	}

	public void init(HLDDelete hld) {
		this.hld = hld;
		tblClause.init(hld.typeOrTbl);
		whereClause.init(hld.hld);
		if (deleteInClause != null) {
			deleteInClause.init(hld);
		}
	}
	
	@Override
	public SqlStatement render() {
		SqlStatement stm = new SqlStatement(hld);
		StrCreator sc = new StrCreator();
		sc.o("DELETE FROM");
		sc.o(tblClause.render(stm));

		if (deleteInClause != null) {
			sc.addStr(deleteInClause.render(stm));
		} else {
			sc.addStr(whereClause.render(stm));
		}

		stm.sql = sc.toString();
		return stm;
	}
}
