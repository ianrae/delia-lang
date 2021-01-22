package org.delia.db.hld;

import org.delia.type.DStructType;

/**
 * Represents a field that is a struct (eg. a relation). eg Customer.addr
 * @author ian
 *
 */
public class RelationField  {
	public DStructType dtype;
	public String fieldName;
	public DStructType fieldType;
	
	public RelationField(DStructType dtype, String field, DStructType fieldType) {
		this.dtype = dtype;
		this.fieldName = field;
		this.fieldType = fieldType;
	}
	
	@Override
	public String toString() {
		String s = String.format("%s.%s:%s", dtype.getName(), fieldName, fieldType.getName());
		return s;
	}
}