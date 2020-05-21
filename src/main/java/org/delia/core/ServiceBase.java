package org.delia.core;

import org.delia.error.ErrorTracker;
import org.delia.log.Log;

/**
 * Base class for many services. Holds a log and error tracker.
 * 
 * @author Ian Rae
 *
 */
public class ServiceBase {
	protected Log log;
	protected ErrorTracker et;
	protected FactoryService factorySvc;

	public ServiceBase(FactoryService factorySvc) {
		this.factorySvc = factorySvc;
		
		if (factorySvc != null) {
			this.log = factorySvc.getLog();
			this.et = factorySvc.getErrorTracker();
		}
	}
}
