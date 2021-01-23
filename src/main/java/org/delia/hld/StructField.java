package org.delia.hld;

import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;

/**
 * Represents a struct field. eg. Customer.firstName field may be a relation.
 * @author ian
 *
 */
public class StructField  {
	public DStructType dtype;
	public String fieldName;
	public DType fieldType;

	public StructField(DStructType dtype, String field, DType fieldType) {
		this.dtype = dtype;
		this.fieldName = field;
		this.fieldType = fieldType;
	}

	@Override
	public String toString() {
		String fldType = BuiltInTypes.convertDTypeNameToDeliaName(fieldType.getName());
		String s = String.format("%s.%s:%s", dtype.getName(), fieldName, fldType);
		return s;
	}
}