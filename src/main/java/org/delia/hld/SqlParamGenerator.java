package org.delia.hld;

import org.delia.core.FactoryService;
import org.delia.hld.cond.FilterVal;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.BlobUtils;
import org.delia.valuebuilder.ScalarValueBuilder;

/**
 * Converts values in SQL WHEN clause to sql format.
 * See ValueHelper for converion of values in INSERT,UPDATE,etc
 * @author ian
 *
 */
public class SqlParamGenerator {
	protected ScalarValueBuilder scalarBuilder;
	
	public SqlParamGenerator(DTypeRegistry registry, FactoryService factorySvc) {
		this.scalarBuilder = factorySvc.createScalarValueBuilder(registry);
	}
	
	/**
	 * Convert to sql representation of data value.
	 * @param fval
	 * @param dtypeHint - actual delia type that we're rendering. However it may be null sometimes.
	 * @return
	 */
	public DValue convert(FilterVal fval, DType dtypeHint) {
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
			if (dtypeHint != null) {
				if (dtypeHint.isShape(Shape.DATE)) {
					return scalarBuilder.buildString(fval.asString()); //TODO: fix this. what is correct date format in sql?
				} else if (dtypeHint.isShape(Shape.BLOB)) {
					//h2 and postgres both use hex format
					String base64Str = fval.asString();
					String hex = BlobUtils.base64ToHexString(base64Str);
					return scalarBuilder.buildString(hex);
				}
			}
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