package org.delia.db.sqlgen;

import org.delia.db.sql.prepared.SqlStatement;

public interface SqlStatementGenerator {
	SqlStatement render();
}
