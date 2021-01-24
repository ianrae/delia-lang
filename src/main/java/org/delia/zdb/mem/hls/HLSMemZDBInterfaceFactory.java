package org.delia.zdb.mem.hls;

import org.delia.core.FactoryService;
import org.delia.db.DBCapabilties;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.mem.MemZDBExecutor;
import org.delia.zdb.mem.MemZDBInterfaceFactory;

public class HLSMemZDBInterfaceFactory extends MemZDBInterfaceFactory {
	
	public HLSMemZDBInterfaceFactory(FactoryService factorySvc) {
		super(factorySvc);
		this.capabilities = new DBCapabilties(false, false, true, true);
	}
	
	@Override
	public ZDBExecutor createExecutor() {
		ZDBExecutor exec = new HLSMemZDBExecutor(factorySvc, this);
		
		if (observerFactory != null) {
			ZDBExecutor observer = observerFactory.createObserver(exec);
			return observer;
		}
		return exec;

	}
}