package org.delia.dbimpl.mem.impl;

import org.delia.core.FactoryService;
import org.delia.type.DTypeRegistry;

import java.util.Map;

//per database stuff
public class DBStuff {
	public SerialProvider serialProvider;
//	public QueryTypeDetector queryDetectorSvc;

    public void init(FactoryService factorySvc, DTypeRegistry registry, Map<String, SerialProvider.SerialGenerator> map) {
//		this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
		if (serialProvider == null) {
			this.serialProvider = new SerialProvider(factorySvc, registry, map);
		} else {
			//we want to keep the serial providers so don't generate ids already used
			this.serialProvider.setRegistry(registry);
		}
    }
}