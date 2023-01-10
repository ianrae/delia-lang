//package org.delia.typebuilder;
//
//import org.delia.type.*;
//
//public class InternalTypeCreator {
//
//	public DStructType createSchemaVersionType(DTypeRegistry registry, String typeName) {
//		DType strType = registry.getType(BuiltInTypes.STRING_SHAPE);
//		DType intType = registry.getType(BuiltInTypes.INTEGER_SHAPE);
//
//		OrderedMap omap = new OrderedMap();
//		omap.add("id", intType, false, true, false, true); //serial
//		omap.add("fingerprint", strType, false, false, false, false);
//		PrimaryKey prikey = new PrimaryKey(new TypePair("id", intType));
//		DStructType dtype = new DStructTypeImpl(Shape.STRUCT, null, typeName, null, omap, prikey);
//		return dtype;
//	}
//	public DStructType createDATType(DTypeRegistry registry, String typeName) {
//		DType strType = registry.getType(BuiltInTypes.STRING_SHAPE);
//		DType intType = registry.getType(BuiltInTypes.INTEGER_SHAPE);
//
//		OrderedMap omap = new OrderedMap();
//		omap.add("id", intType, false, true, false, true); //serial
//		omap.add("tblName", strType, false, false, false, false);
//		omap.add("leftName", strType, false, false, false, false);
//		omap.add("rightName", strType, false, false, false, false);
//		PrimaryKey prikey = new PrimaryKey(new TypePair("id", intType));
//		DStructType dtype = new DStructTypeImpl(Shape.STRUCT, null, typeName, null, omap, prikey);
//		return dtype;
//	}
//
//}