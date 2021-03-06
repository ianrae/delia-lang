package org.delia.db.sqlgen;

import org.delia.db.SqlStatement;
import org.delia.db.sql.StrCreator;
import org.delia.hld.cud.HLDInsert;

public class SqlInsertStatement implements SqlStatementGenerator {

	protected HLDInsert hld;
	protected SqlTableNameClause tblClause;
	protected SqlValueListClause valueClause;
	protected SqlFieldListClause fieldClause;
	
	public SqlInsertStatement(SqlTableNameClause tblClause, SqlFieldListClause fieldClause, SqlValueListClause valueClause) {
		this.tblClause = tblClause;
		this.fieldClause = fieldClause;
		this.valueClause = valueClause;
	}
	
	public void init(HLDInsert hld) {
		this.hld = hld;
		tblClause.init(hld.typeOrTbl);
		fieldClause.init(hld.fieldL);
		valueClause.init(hld.valueL);
	}
	@Override
	public SqlStatement render() {
		SqlStatement stm = new SqlStatement(hld);
		StrCreator sc = new StrCreator();
		sc.o("INSERT INTO");
		sc.o(tblClause.render(stm));
		
		sc.o(fieldClause.render(stm));
		if (!hld.fieldL.isEmpty()) {
			sc.o(valueClause.render(stm));
		}
		
		stm.sql = sc.toString();
		return stm;
	}

}
