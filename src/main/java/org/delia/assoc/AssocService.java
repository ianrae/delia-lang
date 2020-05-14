package org.delia.assoc;

import org.delia.type.DTypeRegistry;

public interface AssocService {

	void assignDATIds(DTypeRegistry registry);
	String getAssocTblName(int datId);
	DatIdMap getDatIdMap();
}
