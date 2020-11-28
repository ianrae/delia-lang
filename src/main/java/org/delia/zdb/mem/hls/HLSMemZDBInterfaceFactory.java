package org.delia.zdb.mem.hls;

import org.delia.core.FactoryService;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.mem.MemZDBInterfaceFactory;

public class HLSMemZDBInterfaceFactory extends MemZDBInterfaceFactory {
	
	public HLSMemZDBInterfaceFactory(FactoryService factorySvc) {
		super(factorySvc);
	}
	
	@Override
	public ZDBExecutor createExecutor() {
		return new HLSMemZDBExecutor(factorySvc, this);
	}
}