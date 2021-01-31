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
import org.delia.hld.HLDFactory;
import org.delia.zdb.DBConnection;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;
import org.delia.zdb.DBObserverFactory;

public class MemDBInterfaceFactory extends ServiceBase implements DBInterfaceFactory {
	protected DBCapabilties capabilities;
	private Map<String,MemDBTable> tableMap; //only one for new
	private Map<String,SerialGenerator> serialMap = new ConcurrentHashMap<>(); //key, nextId values
	protected DBObserverFactory observerFactory;
	private boolean ignoreSimpleSvcSql;
	private HLDFactory hldFactory;

	public MemDBInterfaceFactory(FactoryService factorySvc, HLDFactory hldFactory) {
		super(factorySvc);
		this.capabilities = new DBCapabilties(false, false, false);
		this.hldFactory = hldFactory;
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
	public DBConnection openConnection() {
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
	public DBExecutor createExecutor() {
		DBExecutor exec = new MemDBExecutor(factorySvc, this, hldFactory);
		
		//there is no dbconnection for MEM
		if (observerFactory != null) {
			DBExecutor observer = observerFactory.createObserver(exec, null, ignoreSimpleSvcSql);
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

	@Override
	public void setIgnoreSimpleSvcSql(boolean flag) {
		this.ignoreSimpleSvcSql = flag;
	}
	@Override
	public HLDFactory getHLDFactory() {
		return hldFactory;
	}

}