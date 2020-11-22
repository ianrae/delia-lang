package org.delia.builder;

import org.delia.api.Delia;
import org.delia.api.DeliaFactory;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.DBType;
import org.delia.db.sql.ConnectionString;
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
	private ConnectionString connStr;
	private Log log;
	private LogFactory logFactory;
	private DBType dbType;
	
	public static DeliaBuilder withConnection(ConnectionInfo info) {
		theSingleton = new DeliaBuilder();
		theSingleton.info = info;
		return theSingleton;
	}
	public static DeliaBuilder withConnection(ConnectionString connStr, DBType dbType) {
		theSingleton = new DeliaBuilder();
		theSingleton.connStr = connStr;
		theSingleton.dbType = dbType;
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
		if (info != null) {
			Delia delia = DeliaFactory.create(info, log, factorySvc);
			return delia;
		} else {
			Delia delia = DeliaFactory.create(connStr, dbType, log, factorySvc);
			return delia;
		}
	}
}
