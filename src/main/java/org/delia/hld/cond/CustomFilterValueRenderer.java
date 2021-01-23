package org.delia.hld.cond;

import org.delia.db.sql.prepared.SqlStatement;
import org.delia.hld.HLDAliasBuilderAdapter;
import org.delia.hld.HLDQuery;
import org.delia.hld.SqlParamGenerator;

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

	void assignAliases(Object obj, HLDQuery hld, HLDAliasBuilderAdapter hldAliasBuilderAdapter);
}
