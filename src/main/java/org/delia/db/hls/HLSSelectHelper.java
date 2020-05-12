package org.delia.db.hls;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.BuiltInTypes;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;

public class HLSSelectHelper extends ServiceBase {
	protected DTypeRegistry registry;

	public HLSSelectHelper(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
	}
	
	public ResultTypeInfo getSelectResultType(HLSQueryStatement hls) {
		int n = hls.hlspanL.size();
		HLSQuerySpan hlspan = hls.hlspanL.get(n -  1); //TODO: sometimes not last one!!!
		
		ResultTypeInfo resultTypeInfo = new ResultTypeInfo();
		resultTypeInfo.logicalType = hlspan.resultType;
		if (isExistsFn(hls)) {
			resultTypeInfo.physicalType = registry.getType(BuiltInTypes.LONG_SHAPE);
		} else {
			resultTypeInfo.physicalType = hlspan.resultType;
			
		}
		return resultTypeInfo;
	}

	private boolean isExistsFn(HLSQueryStatement hls) {
		HLSQuerySpan hlspan = hls.getMainHLSSpan();
		return hlspan.hasFunction("exists");
	}
	

}