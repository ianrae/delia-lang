package org.delia.db.hld;

import org.delia.core.FactoryService;
import org.delia.db.hld.cond.FilterVal;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;

public class SqlParamGenerator {
	protected ScalarValueBuilder scalarBuilder;
	
	public SqlParamGenerator(DTypeRegistry registry, FactoryService factorySvc) {
		this.scalarBuilder = factorySvc.createScalarValueBuilder(registry);
	}
	public DValue convert(FilterVal fval) {
		if (fval.actualDateVal != null) {
			return fval.actualDateVal;
		}
		
		switch(fval.valType) {
		case BOOLEAN:
			return scalarBuilder.buildBoolean(fval.asBoolean());
		case INT:
			return scalarBuilder.buildInt(fval.asInt());
		case LONG:
			return scalarBuilder.buildLong(fval.asLong());
		case NUMBER:
			return scalarBuilder.buildNumber(fval.asNumber());
		case STRING:
			return scalarBuilder.buildString(fval.asString());
			//TODO: implement date!!
		case FUNCTION:
		case SYMBOL:
		case NULL:
		case SYMBOLCHAIN:
			default:
				return null;
		}
	}
}