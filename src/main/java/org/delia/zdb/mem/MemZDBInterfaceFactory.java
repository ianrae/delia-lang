package org.delia.zdb.mem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBCapabilties;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBTable;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.DValueImpl;
import org.delia.type.TypeReplaceSpec;
import org.delia.zdb.ZDBConnection;
import org.delia.zdb.ZDBInterfaceFactory;

public class MemZDBInterfaceFactory extends ServiceBase implements ZDBInterfaceFactory {
	private DBCapabilties capabilities;
	private Map<String,MemDBTable> tableMap; //only one for new

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
	public void performTypeReplacement(TypeReplaceSpec spec) {
		for (String typeName: tableMap.keySet()) {
			MemDBTable tbl = tableMap.get(typeName);
			for(DValue dval: tbl.rowL) {
				DType dtype = dval.getType();

				//in addition the DValues stored here may be from a previous entire run
				//of Runner (and its registry).
				//so also check by name
				boolean shouldReplace = dtype.getName().equals(spec.newType.getName());

				if (shouldReplace || spec.needsReplacement(this, dtype)) {
					DValueImpl impl = (DValueImpl) dval;
					impl.forceType(spec.newType);
				} else {
					dtype.performTypeReplacement(spec);
				}
			}
		}
	}

}