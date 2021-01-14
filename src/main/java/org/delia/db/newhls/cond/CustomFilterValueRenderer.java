package org.delia.db.newhls.cond;

import org.delia.db.newhls.SqlParamGenerator;
import org.delia.db.sql.prepared.SqlStatement;

/**
 * Used for adding inner selects
 * @author ian
 *
 */
public interface CustomFilterValueRenderer {
	String render(FilterVal val1, SqlParamGenerator paramGen, SqlStatement stm);
}
