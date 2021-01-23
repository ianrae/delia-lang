package org.delia.hld;

import org.delia.type.DStructType;
import org.delia.type.DType;

/**
 * Represents a struct field. eg. Customer.firstName field may be a relation.
 * @author ian
 *
 */
public class StructFieldOpt  {
	public DStructType dtype;
	public String fieldName; //can be null
	public DType fieldType; //can be null

	public String alias; //added later
	
	public StructFieldOpt(DStructType dtype, String field, DType fieldType) {
		this.dtype = dtype;
		this.fieldName = field;
		this.fieldType = fieldType;
	}

	@Override
	public String toString() {
		String fstr = fieldType == null ? "" : fieldType.getName();
		String s = String.format("%s.%s:%s", dtype.getName(), fieldName, fstr);
		return s;
	}
}