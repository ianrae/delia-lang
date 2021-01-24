package org.delia.zdb.mem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBCapabilties;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBTable;
import org.delia.db.memdb.SerialProvider.SerialGenerator;
import org.delia.zdb.DBObserverFactory;
import org.delia.zdb.ZDBConnection;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;

public class MemZDBInterfaceFactory extends ServiceBase implements ZDBInterfaceFactory {
	protected DBCapabilties capabilities;
	private Map<String,MemDBTable> tableMap; //only one for new
	private Map<String,SerialGenerator> serialMap = new ConcurrentHashMap<>(); //key, nextId values
	private DBObserverFactory observerFactory;

	public MemZDBInterfaceFactory(FactoryService factorySvc) {
		super(factorySvc);
		this.capabilities = new DBCapabilties(false, false, false, false);
	}
	
	public Map<String,MemDBTable> createSingleMemDB() {
		if (tableMap == null) {
			tableMap = new ConcurrentHashMap<>();
		}
		return tableMap;
	}


	@Override
	public DBType getDBType() {
		return DBType.MEM;
	}

	@Override
	public DBCapabilties getCapabilities() {
		return capabilities;
	}

	@Override
	public ZDBConnection openConnection() {
		return null; //no connection for MEM
	}

	@Override
	public boolean isSQLLoggingEnabled() {
		return false;
	}

	@Override
	public void enableSQLLogging(boolean b) {
		//not supported
	}
	
	@Override
	public ZDBExecutor createExecutor() {
		ZDBExecutor exec = new MemZDBExecutor(factorySvc, this);
		
		if (observerFactory != null) {
			ZDBExecutor observer = observerFactory.createObserver(exec);
			return observer;
		}
		return exec;
	}
	
	
	public Map<String, SerialGenerator> getSerialMap() {
		return serialMap;
	}

	@Override
	public DBErrorConverter getDBErrorConverter() {
		return null;
	}
	@Override
	public void setDBErrorConverter(DBErrorConverter errorConverter) {
		//not used this.errorConverter = errorConverter;
	}

	@Override
	public void setObserverFactory(DBObserverFactory observerFactory) {
		this.observerFactory = observerFactory;
	}

	@Override
	public DBObserverFactory getObserverFactory() {
		return observerFactory;
	}

}