package org.delia.sql;

import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;


/**
 * The return type of an sql statement.
 * Logical type is the type that Delia is expecting. eg Customer.count() would be long
 * Physical type is what's actually in the result set. (for exists() physical type is long but logical type is boolean)
 * @author irae
 *
 */
public class ResultTypeInfo {
	public DType logicalType;
	public DType physicalType;

	public boolean isScalarShape() {
		return logicalType.isScalarShape();
	}

	public boolean needPhysicalToLogicalMapping() {
		return logicalType != physicalType;
	}

	//TODO: do we need this?
	public DValue mapPhysicalToLogicalValue(DValue dval, ScalarValueBuilder builder) {
 		//only one now is exists
		if (Shape.BOOLEAN.equals(logicalType.getShape())) {
			long n = dval == null ? 0L : dval.asLong();
			return builder.buildBoolean(n != 0L);
		}
		DeliaExceptionHelper.throwError("unsupported-logical-to-physical-mapping", "don't support shape %s", logicalType.getShape().name());
		return null;
	}
}
