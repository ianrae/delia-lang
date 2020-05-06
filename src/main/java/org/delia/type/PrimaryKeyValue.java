package org.delia.type;

public class PrimaryKeyValue {

	private PrimaryKey pkey;
	private DValue value;
	private DValue structVal;

	public PrimaryKeyValue(DValue dval) {
		this.structVal = dval;
		DStructType structType = (DStructType) dval.getType();
		PrimaryKey pkey = structType.getPrimaryKey();
		if (pkey == null) {
			this.pkey = null;
		} else {
			this.pkey = pkey;
			//TODO: support composite keys later!!
		}
	}
	
	public DValue getKeyValue() {
		if (pkey == null) {
			return null;
		} else if (value == null) {
			//get lazily
			this.value = structVal.asStruct().getField(pkey.getFieldName());
		}
		return this.value;
	}
}
