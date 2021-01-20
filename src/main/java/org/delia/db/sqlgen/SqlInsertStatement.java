package org.delia.db.sqlgen;

import org.delia.db.newhls.cud.HLDInsert;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;

public class SqlInsertStatement implements SqlStatementGenerator {

	private HLDInsert hld;
	private SqlTableNameClause tblClause;
	private SqlValueListClause valueClause;
	private SqlFieldListClause fieldClause;
	
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
		sc.o(valueClause.render(stm));
		
		stm.sql = sc.toString();
		return stm;
	}

}
