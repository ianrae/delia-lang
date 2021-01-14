package org.delia.db.newhls.cond;

import org.delia.db.newhls.SqlParamGenerator;
import org.delia.db.sql.prepared.SqlStatement;

/**
 * Used for adding inner selects
 * @author ian
 *
 */
public interface CustomFilterValueRenderer {
	/**
	 * 
	 * @param obj  could be a FilterVal or a OpFilterCond
	 * @param paramGen
	 * @param stm
	 * @return
	 */
	String render(Object obj, SqlParamGenerator paramGen, SqlStatement stm);
}
