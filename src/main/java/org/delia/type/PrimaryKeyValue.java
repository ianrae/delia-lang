package org.delia.type;

import org.delia.util.DeliaExceptionHelper;

/**
 * Represents the primary value(s) of a DValue object
 * It's not required that a DValue has a primary key value, but almost all do.
 * The primary key is either:
 *  -a single DValue if there is a single primaryKey field
 *  -a list of DValues if there is a composite primaryKey field
 */
public class PrimaryKeyValue {

	private PrimaryKey primaryKey;
	private DValue value;
	private DValue structVal;

	public PrimaryKeyValue(DValue dval) {
		this.structVal = dval;
		DStructType structType = (DStructType) dval.getType();
		PrimaryKey pkey = structType.getPrimaryKey();
		if (pkey == null) {
			this.primaryKey = null;
		} else {
			this.primaryKey = pkey;
			//TODO: support composite keys later!!
		}
	}
	
	public DValue getKeyValue() {
		if (primaryKey == null) {
			return null;
		} else if (value == null) {
			//get lazily
			if (primaryKey.isMultiple()) {
				DeliaExceptionHelper.throwError("unexpected.composite.primarykey",
						"Can't call getKeyValue when composite key. Type='%s'", structVal.getType().getName());
			}
			this.value = structVal.asStruct().getField(primaryKey.getFieldName());
		}
		return this.value;
	}
}
