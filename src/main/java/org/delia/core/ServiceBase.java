package org.delia.core;

import org.delia.error.ErrorTracker;
import org.delia.log.DeliaLog;

/**
 * Base class for many services. Holds a log and error tracker.
 * 
 * @author Ian Rae
 *
 */
public class ServiceBase {
	protected DeliaLog log;
	protected ErrorTracker et; //TODO get rid of factoryservice.et. need local one per session or even local per use
	protected FactoryService factorySvc;

	public ServiceBase(FactoryService factorySvc) {
		this.factorySvc = factorySvc;
		
		if (factorySvc != null) {
			this.log = factorySvc.getLog();
			this.et = factorySvc.getErrorTracker();
		}
	}
}
