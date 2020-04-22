package org.delia.builder;

import org.delia.api.Delia;
import org.delia.api.DeliaFactory;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.log.LogFactory;
import org.delia.log.SimpleLog;

/**
 * Main builder for creating your application's Delia object.
 * 
 * @author Ian Rae
 *
 */
public class DeliaBuilder {
	private static DeliaBuilder theSingleton;
	private ConnectionInfo info;
	private Log log;
	private LogFactory logFactory;
	
	public static DeliaBuilder withConnection(ConnectionInfo info) {
		theSingleton = new DeliaBuilder();
		theSingleton.info = info;
		return theSingleton;
	}
	public DeliaBuilder log(Log log) {
		this.log = log;
		return this;
	}
	public DeliaBuilder log(LogFactory logFactory) {
		this.logFactory = logFactory;
		return this;
	}
	
	public Delia build() {
		if (log == null) {
			if (logFactory != null) {
				log = logFactory.create("delia-logger"); //a single logger for Delia
			} else {
				log = new SimpleLog();
			}
		}
		ErrorTracker et = new SimpleErrorTracker(log);
		FactoryService factorySvc = new FactoryServiceImpl(log, et);
		Delia delia = DeliaFactory.create(info, log, factorySvc);
		return delia;
	}
}
