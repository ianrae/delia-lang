//package org.delia.type;
//
//public class PrimaryKeyValue {
//
//	private PrimaryKey pkey;
//
//	public PrimaryKeyValue(DValue dval) {
//		PrimaryKey pkey = dval.getType().getPrimaryKey();
//		if (pkey == null) {
//			this.pkey = null;
//		} else {
//			this.pkey = pkey;
//		}
//	}
//	
//	public DValue getKeyValue() {
//		return pkey == null ? null : dval.asStruct.getField(pkey.getName());
//	}
//}
