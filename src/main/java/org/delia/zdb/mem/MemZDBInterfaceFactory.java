package org.delia.zdb.mem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBCapabilties;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBTable;
import org.delia.db.memdb.SerialProvider.SerialGenerator;
import org.delia.zdb.ZDBConnection;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;

public class MemZDBInterfaceFactory extends ServiceBase implements ZDBInterfaceFactory {
	private DBCapabilties capabilities;
	private Map<String,MemDBTable> tableMap; //only one for new
	private Map<String,SerialGenerator> serialMap = new ConcurrentHashMap<>(); //key, nextId values
	

	public MemZDBInterfaceFactory(FactoryService factorySvc) {
		super(factorySvc);
		this.capabilities = new DBCapabilties(false, false, false, false);
		this.capabilities.setRequiresTypeReplacementProcessing(true);
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
		return new MemZDBExecutor(factorySvc, this);
	}
	
	
	public Map<String, SerialGenerator> getSerialMap() {
		return serialMap;
	}

}