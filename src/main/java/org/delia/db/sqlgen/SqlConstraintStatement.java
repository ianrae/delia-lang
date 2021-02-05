package org.delia.db.sqlgen;

import org.delia.db.SqlStatement;
import org.delia.db.schema.modify.SchemaChangeOperation;
import org.delia.db.sql.StrCreator;

public class SqlConstraintStatement implements SqlStatementGenerator {

	private SchemaChangeOperation op;

	public SqlConstraintStatement() {
	}
	
	public void init(SchemaChangeOperation op) {
		this.op = op;
	}
	
	@Override
	public SqlStatement render() {
		SqlStatement stm = new SqlStatement(this);
		StrCreator sc = new StrCreator();
		sc.o("xxxINSERT INTO");
		
		stm.sql = sc.toString();
		return stm;
	}

}
