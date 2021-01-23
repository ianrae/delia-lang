package org.delia.db.sqlgen;

import org.delia.db.SqlStatement;

public interface SqlClauseGenerator {
	String render(SqlStatement stm);
}
