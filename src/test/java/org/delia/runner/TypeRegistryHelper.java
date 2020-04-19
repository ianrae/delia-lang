package org.delia.runner;

import org.delia.type.BuiltInTypes;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DTypeRegistryBuilder;


public class TypeRegistryHelper {
	
	private static DTypeRegistry registry;
	
	public static DTypeRegistry init() {
		DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
		registryBuilder.init();
		registry = registryBuilder.getRegistry();
		return registry;
	}
	
	public static DType getStringType() {
		return getStringType(registry);
	}
	public static DType getIntType() {
		return getIntType(registry);
	}
	public static DType getLongType() {
		return getLongType(registry);
	}
	public static DType getNumberType() {
		return getNumberType(registry);
	}
	public static DType getBooleanType() {
		return getBooleanType(registry);
	}
	public static DType getRelationType() {
		return getRelationType(registry);
	}
	public static DType getStringType(DTypeRegistry reg) {
		DType type = reg.getType(BuiltInTypes.STRING_SHAPE);
		return type;
	}
	public static DType getIntType(DTypeRegistry reg) {
		DType type = reg.getType(BuiltInTypes.INTEGER_SHAPE);
		return type;
	}
	public static DType getLongType(DTypeRegistry reg) {
		DType type = reg.getType(BuiltInTypes.LONG_SHAPE);
		return type;
	}
	public static DType getNumberType(DTypeRegistry reg) {
		DType type = reg.getType(BuiltInTypes.NUMBER_SHAPE);
		return type;
	}
	public static DType getBooleanType(DTypeRegistry reg) {
		DType type = reg.getType(BuiltInTypes.BOOLEAN_SHAPE);
		return type;
	}
	public static DType getRelationType(DTypeRegistry reg) {
		DType type = reg.getType(BuiltInTypes.RELATION_SHAPE);
		return type;
	}
}
