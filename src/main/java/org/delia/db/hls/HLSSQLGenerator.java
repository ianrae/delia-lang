package org.delia.db.hls;

import org.delia.type.DTypeRegistry;

public interface HLSSQLGenerator {
	String buildSQL(HLSQueryStatement hls);
	String processOneStatement(HLSQuerySpan hlspan, boolean forceAllFields);
	void setRegistry(DTypeRegistry registry);
}
