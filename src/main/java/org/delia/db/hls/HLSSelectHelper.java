package org.delia.db.hls;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;

public class HLSSelectHelper extends ServiceBase {
	protected DTypeRegistry registry;

	public HLSSelectHelper(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
	}
	
	public DType getSelectResultType(HLSQueryStatement hls) {
		int n = hls.hlspanL.size();
		HLSQuerySpan hlspan = hls.hlspanL.get(n -  1); //TODO: sometimes not last one!!!
		return hlspan.resultType;
	}
	

}