package org.delia.zdb.core.mem;

import org.delia.core.FactoryService;
import org.delia.db.memdb.SerialProvider;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.type.DTypeRegistry;

//per database stuff
public class ZStuff {
	public SerialProvider serialProvider;
	public QueryTypeDetector queryDetectorSvc;

	public void init(FactoryService factorySvc, DTypeRegistry registry) {
		this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
		if (serialProvider == null) {
			this.serialProvider = new SerialProvider(factorySvc, registry);
		} else {
			//we want to keep the serial providers so don't generate ids already used
			this.serialProvider.setRegistry(registry);
		}
	}
}