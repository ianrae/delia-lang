package org.delia.typebuilder;

import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.OrderedMap;
import org.delia.type.PrimaryKey;
import org.delia.type.Shape;
import org.delia.type.TypePair;

public class FakeTypeCreator {
	
	public void createFakeTypes(DTypeRegistry registry) {
		DType strType = registry.getType(BuiltInTypes.STRING_SHAPE);
		DType intType = registry.getType(BuiltInTypes.INTEGER_SHAPE);
		DType boolType = registry.getType(BuiltInTypes.BOOLEAN_SHAPE);
		DType relType = registry.getType(BuiltInTypes.RELATION_SHAPE);
		
		OrderedMap omap = new OrderedMap();
		omap.add("id",        intType, false, true, false, false);
		omap.add("firstName", strType, false, false, false, false);
		omap.add("lastName",  strType, true, false, false, false);
		omap.add("points",    intType, true, false, false, false);
		omap.add("flag",     boolType, true, false, false, false);
		
		PrimaryKey prikey = new PrimaryKey(new TypePair("id", intType));
		DStructType dtype = new DStructType(Shape.STRUCT, "Customer", null, omap, prikey);
		
		registry.add("Customer", dtype);
	}
	
	public void createDeptAndEmployee(DTypeRegistry registry) {
		DType strType = registry.getType(BuiltInTypes.STRING_SHAPE);
		DType intType = registry.getType(BuiltInTypes.INTEGER_SHAPE);
		
		//Dept
		OrderedMap omap = new OrderedMap();
		omap.add("deptId", intType, false, true, false, false);
		omap.add("name", strType, false, false, false, false);
		PrimaryKey prikey = new PrimaryKey(new TypePair("deptId", intType));
		DStructType dtype = new DStructType(Shape.STRUCT, "Dept", null, omap, prikey);
		DStructType deptType = dtype;
		registry.add("Dept", dtype);
		
		omap = new OrderedMap();
		omap.add("id", intType, false, true, false, false);
		omap.add("firstName", strType, false, false, false, false);
		omap.add("dept", deptType, false, false, false, false);
		prikey = new PrimaryKey(new TypePair("id", intType));
		dtype = new DStructType(Shape.STRUCT, "Employee", null, omap, prikey);
		registry.add("Employee", dtype);
	}
	
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
}