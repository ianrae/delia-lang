package org.delia.zdb.core.mem;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBCapabilties;
import org.delia.db.DBType;
import org.delia.zdb.core.ZDBConnection;
import org.delia.zdb.core.ZDBInterfaceFactory;

public class MemZDBInterfaceFactory extends ServiceBase implements ZDBInterfaceFactory {
	private DBCapabilties capabilities;

	public MemZDBInterfaceFactory(FactoryService factorySvc) {
		super(factorySvc);
		this.capabilities = new DBCapabilties(false, false, false, false);
		this.capabilities.setRequiresTypeReplacementProcessing(true);
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
}