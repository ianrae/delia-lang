package org.delia.db.postgres;

import org.delia.db.SqlStatement;
import org.delia.db.sqlgen.SqlFieldListClause;
import org.delia.db.sqlgen.SqlMergeUsingStatement;
import org.delia.db.sqlgen.SqlTableNameClause;
import org.delia.db.sqlgen.SqlValueListClause;
import org.delia.hld.cud.HLDUpdate;

public class PostgresSqlMergeUsingStatement extends SqlMergeUsingStatement {

	private PostgresSqlMergeIntoStatement innerGen;
	
	public PostgresSqlMergeUsingStatement(SqlTableNameClause tblClause, SqlFieldListClause fieldClause, SqlValueListClause valueClause) {
		super(tblClause, fieldClause, valueClause);
		this.innerGen = new PostgresSqlMergeIntoStatement(tblClause, fieldClause, valueClause);
	}
	
	public void init(HLDUpdate hld) {
		this.hld = hld;
		this.innerGen.init(hld);
	}
	@Override
	public SqlStatement render() {
		return innerGen.render();
	}

}
