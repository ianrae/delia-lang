package org.delia.db.sqlgen;

import org.delia.db.sql.prepared.SqlStatement;

public interface SqlClauseGenerator {
	String render(SqlStatement stm);
}
