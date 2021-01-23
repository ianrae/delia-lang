package org.delia.hld;

import org.delia.hld.cud.HLDDelete;
import org.delia.hld.cud.HLDInsert;
import org.delia.hld.cud.HLDUpdate;
import org.delia.hld.simple.SimpleBase;

/**
 * @author ian
 *
 */
public interface HLDAliasBuilderAdapter {

	void assignAliases(HLDQuery hld);
	void assignAliases(HLDInsert hld);
	void assignAliases(HLDUpdate hld);
	void assignAliases(HLDDelete hld);
	void assignAliases(SimpleBase simple);
	void pushAliasScope(String scope);
	void popAliasScope();
	String createAlias();
	boolean isOutputAliases();
	void setOutputAliases(boolean outputAliases);
}