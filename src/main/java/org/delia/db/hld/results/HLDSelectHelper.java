package org.delia.db.hld.results;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.hld.HLDQuery;
import org.delia.db.hld.HLDQueryStatement;
import org.delia.db.hld.QueryFnSpec;
import org.delia.db.hls.ResultTypeInfo;
import org.delia.type.BuiltInTypes;
import org.delia.type.DTypeRegistry;

public class HLDSelectHelper extends ServiceBase {
	protected DTypeRegistry registry;

	public HLDSelectHelper(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
	}
	
	public ResultTypeInfo getSelectResultType(HLDQueryStatement hld) {
		HLDQuery hldquery = hld.hldquery;
		
		ResultTypeInfo resultTypeInfo = new ResultTypeInfo();
		resultTypeInfo.logicalType = hldquery.resultType;
		if (isExistsFn(hld)) {
			resultTypeInfo.physicalType = registry.getType(BuiltInTypes.LONG_SHAPE);
		} else {
			resultTypeInfo.physicalType = hldquery.resultType;
			
		}
		return resultTypeInfo;
	}

	private boolean isExistsFn(HLDQueryStatement hld) {
		if (hld.hldquery.funcL.isEmpty()) {
			return false;
		}
		int n = hld.hldquery.funcL.size();
		QueryFnSpec spec = hld.hldquery.funcL.get(n - 1);
		return spec.isFn("exists");
	}
	

}