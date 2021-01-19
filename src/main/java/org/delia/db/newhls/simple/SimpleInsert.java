package org.delia.db.newhls.simple;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.newhls.HLDAliasBuilderAdapter;
import org.delia.db.newhls.SqlColumn;
import org.delia.db.newhls.cud.HLDInsert;

/**
 * A simple INSERT statement.
 * @author ian
 *
 */
public class SimpleInsert extends SimpleBase {
	public List<SqlColumn> fieldL = new ArrayList<>();
	public HLDInsert hld; //for aliases and values
	
	@Override
	public void assignAliases(HLDAliasBuilderAdapter aliasBuilder) {
		boolean save = aliasBuilder.isOutputAliases();
		aliasBuilder.setOutputAliases(false);
		
		aliasBuilder.assignAliases(hld);
		for(SqlColumn column: fieldL) {
			column.alias = hld.getMainAlias();
		}
		tblFrag.alias = hld.getMainAlias();
		
		aliasBuilder.setOutputAliases(save);
	}

}