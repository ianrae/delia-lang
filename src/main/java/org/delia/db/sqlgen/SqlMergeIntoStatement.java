package org.delia.db.sqlgen;

import org.delia.db.newhls.cud.HLDUpdate;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;

public class SqlMergeIntoStatement implements SqlStatementGenerator {

	private HLDUpdate hld;
	private SqlTableNameClause tblClause;
	private SqlValueListClause valueClause;
	
	public SqlMergeIntoStatement(SqlTableNameClause tblClause, SqlValueListClause valueClause) {
		this.tblClause = tblClause;
		this.valueClause = valueClause;
	}
	
	public void init(HLDUpdate hld) {
		this.hld = hld;
		tblClause.init(hld.typeOrTbl);
		valueClause.init(hld.valueL);
	}
	@Override
	public SqlStatement render() {
		SqlStatement stm = new SqlStatement(hld);
		StrCreator sc = new StrCreator();
		sc.o("MERGE INTO");
		sc.o(tblClause.render(stm));
		
		sc.o(" KEY(%s)", hld.mergeKey);
		sc.o(valueClause.render(stm));
		
		stm.sql = sc.toString();
		return stm;
	}

}
