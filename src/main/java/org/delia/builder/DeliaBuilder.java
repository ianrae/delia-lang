package org.delia.builder;

import org.delia.Delia;
import org.delia.DeliaFactory;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.log.LogFactory;
import org.delia.log.SimpleLog;
import org.delia.zdb.DBInterfaceFactory;

/**
 * Main builder for creating your application's Delia object.
 * 
 * @author Ian Rae
 *
 */
public class DeliaBuilder {
	private static DeliaBuilder theSingleton;
//	private ConnectionInfo info;
	private ConnectionDefinition connStr;
	private Log log;
	private LogFactory logFactory;
	
//	public static DeliaBuilder withConnection(ConnectionInfo info) {
//		theSingleton = new DeliaBuilder();
//		theSingleton.info = info;
//		return theSingleton;
//	}
	public static DeliaBuilder withConnection(ConnectionDefinition connStr) {
		theSingleton = new DeliaBuilder();
		theSingleton.connStr = connStr;
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
		FactoryService factorySvc = new FactoryServiceImpl(log, et, logFactory);
//		if (info != null) {
//			Delia delia = DeliaFactory.create(info, log, factorySvc);
//			return delia;
//		} else {
			Delia delia = DeliaFactory.create(connStr, log, factorySvc);
			return delia;
//		}
	}

	/**
	 * Ignores connection and builds with the given dbInterface
	 * @param dbInterface
	 * @return
	 */
	public Delia buildEx(DBInterfaceFactory dbInterface, FactoryService factorySvc) {
		Delia delia = DeliaFactory.create(dbInterface, log, factorySvc);
		return delia;
	}
	public FactoryService createFactorySvcEx() {
		if (log == null) {
			if (logFactory != null) {
				log = logFactory.create("delia-logger"); //a single logger for Delia
			} else {
				log = new SimpleLog();
			}
		}
		ErrorTracker et = new SimpleErrorTracker(log);
		FactoryService factorySvc = new FactoryServiceImpl(log, et, logFactory);
		return factorySvc;
	}
	
}
