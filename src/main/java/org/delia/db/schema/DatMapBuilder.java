package org.delia.db.schema;

import org.delia.assoc.DatIdMap;

public interface DatMapBuilder {
	DatIdMap buildDatIdMapFromDBFingerprint();
}
