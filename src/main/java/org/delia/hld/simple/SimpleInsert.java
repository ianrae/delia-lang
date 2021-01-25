package org.delia.hld.simple;

import java.util.ArrayList;
import java.util.List;

import org.delia.hld.HLDAliasBuilderAdapter;
import org.delia.hld.SqlColumn;
import org.delia.hld.cud.HLDInsert;

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
		this.outputAliases = true;
//		boolean save = aliasBuilder.isOutputAliases();
//		aliasBuilder.setOutputAliases(false);
		
		aliasBuilder.assignAliases(hld);
		for(SqlColumn column: fieldL) {
			column.alias = assign(hld.getMainAlias());
		}
		tblFrag.alias = assign(hld.getMainAlias());
		
//		aliasBuilder.setOutputAliases(save);
	}

}