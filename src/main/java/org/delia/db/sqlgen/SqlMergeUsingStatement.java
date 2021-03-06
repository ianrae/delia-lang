package org.delia.db.sqlgen;

import org.delia.db.SqlStatement;
import org.delia.db.sql.StrCreator;
import org.delia.hld.cud.HLDUpdate;

public class SqlMergeUsingStatement implements SqlStatementGenerator {

	protected HLDUpdate hld;
	protected SqlTableNameClause tblClause;
	protected SqlValueListClause valueClause;
	protected SqlFieldListClause fieldClause;
	
	public SqlMergeUsingStatement(SqlTableNameClause tblClause, SqlFieldListClause fieldClause, SqlValueListClause valueClause) {
		this.tblClause = tblClause;
		this.fieldClause = fieldClause;
		this.valueClause = valueClause;
	}
	
	public void init(HLDUpdate hld) {
		this.hld = hld;
		tblClause.init(hld.typeOrTbl);
		fieldClause.init(hld.fieldL);
		valueClause.init(hld.valueL);
	}
	@Override
	public SqlStatement render() {
		SqlStatement stm = new SqlStatement(hld);
		StrCreator sc = new StrCreator();
		sc.o("MERGE INTO");
		hld.typeOrTbl.alias = null;
		sc.o(tblClause.render(stm));
		String alias2 = "t9"; //TODO fix better
		
		sc.o(" USING (SELECT %s FROM %s) AS %s", hld.mergePKField, hld.typeOrTbl.getTblName(), alias2);
		sc.o(" ON %s = %s.%s", hld.mergePKField, alias2, hld.mergePKField);
		
		sc.o(" WHEN NOT MATCHED THEN INSERT");
		sc.o(fieldClause.render(stm));

		sc.o(valueClause.render(stm));
		
		stm.sql = sc.toString();
		return stm;
	}

}
