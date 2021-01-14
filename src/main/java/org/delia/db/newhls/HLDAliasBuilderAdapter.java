package org.delia.db.newhls;

import org.delia.db.newhls.cud.HLDDelete;
import org.delia.db.newhls.cud.HLDInsert;
import org.delia.db.newhls.cud.HLDUpdate;

/**
 * @author ian
 *
 */
public interface HLDAliasBuilderAdapter {

	void assignAliases(HLDQuery hld);
	void assignAliases(HLDInsert hld);
	void assignAliases(HLDUpdate hld);
	void assignAliases(HLDDelete hld);
}