package org.delia.typebuilder;

import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.OrderedMap;
import org.delia.type.PrimaryKey;
import org.delia.type.Shape;
import org.delia.type.TypePair;

public class InternalTypeCreator {
	
	public DStructType createSchemaVersionType(DTypeRegistry registry, String typeName) {
		DType strType = registry.getType(BuiltInTypes.STRING_SHAPE);
		DType intType = registry.getType(BuiltInTypes.INTEGER_SHAPE);
		
		OrderedMap omap = new OrderedMap();
		omap.add("id", intType, false, true, false, true); //serial
		omap.add("fingerprint", strType, false, false, false, false);
		PrimaryKey prikey = new PrimaryKey(new TypePair("id", intType));
		DStructType dtype = new DStructType(Shape.STRUCT, typeName, null, omap, prikey);
		return dtype;
	}
	public DStructType createDATType(DTypeRegistry registry, String typeName) {
		DType strType = registry.getType(BuiltInTypes.STRING_SHAPE);
		DType intType = registry.getType(BuiltInTypes.INTEGER_SHAPE);
		
		OrderedMap omap = new OrderedMap();
		omap.add("id", intType, false, true, false, true); //serial
		omap.add("tblName", strType, false, false, false, false);
		omap.add("left", strType, false, false, false, false);
		omap.add("right", strType, false, false, false, false);
		PrimaryKey prikey = new PrimaryKey(new TypePair("id", intType));
		DStructType dtype = new DStructType(Shape.STRUCT, typeName, null, omap, prikey);
		return dtype;
	}
	
}