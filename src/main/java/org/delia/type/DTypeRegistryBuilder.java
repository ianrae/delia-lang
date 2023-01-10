package org.delia.type;

public class DTypeRegistryBuilder {
	private DTypeRegistry registry = new DTypeRegistry();

	public DTypeRegistryBuilder() {
	}
	//used for sizeof ints
	public DTypeRegistryBuilder(DTypeRegistry registry) {
		this.registry = registry;
	}

	public void init() {
		String name = BuiltInTypes.INTEGER_SHAPE.name();
		DType type = new DTypeImpl(Shape.INTEGER, null, name, null);
        registerType(name, type);

//        name = BuiltInTypes.LONG_SHAPE.name();
//        type = new DType(Shape.LONG, name, null);
//        registerType(name, type);

        name = BuiltInTypes.NUMBER_SHAPE.name();
        type = new DTypeImpl(Shape.NUMBER, null, name, null);
        registerType(name, type);

		name = BuiltInTypes.STRING_SHAPE.name();
		type = new DTypeImpl(Shape.STRING, null, name, null);
        registerType(name, type);

		name = BuiltInTypes.BOOLEAN_SHAPE.name();
		type = new DTypeImpl(Shape.BOOLEAN, null, name, null);
        registerType(name, type);

		name = BuiltInTypes.DATE_SHAPE.name();
		type = new DTypeImpl(Shape.DATE, null, name, null);
        registerType(name, type);
        
		name = BuiltInTypes.BLOB_SHAPE.name();
		type = new DTypeImpl(Shape.BLOB, null, name, null);
        registerType(name, type);
        
        name = BuiltInTypes.RELATION_SHAPE.name();
        type = new DTypeImpl(Shape.RELATION, null, name, null);
        registerType(name, type);
	}
	
    private void registerType(String typeName, DType dtype) {
        registry.registerType(DTypeRegistry.createDTypeName(typeName), dtype);
    }

	public DTypeRegistry getRegistry() {
		return registry;
	}

	/**
	 * If we detect int types with sizeof, define some additional types, one
	 * for each possible sizeof
	 */
	public void registerSizeOfInts() {
		DType intType = registry.getType(BuiltInTypes.INTEGER_SHAPE);

		registerIntType(BuiltInSizeofIntTypes.INTEGER_8.name(), intType, EffectiveShape.EFFECTIVE_INT);
		registerIntType(BuiltInSizeofIntTypes.INTEGER_16.name(), intType, EffectiveShape.EFFECTIVE_INT);
		registerIntType(BuiltInSizeofIntTypes.INTEGER_32.name(), intType, EffectiveShape.EFFECTIVE_INT);
		registerIntType(BuiltInSizeofIntTypes.INTEGER_64.name(), intType, EffectiveShape.EFFECTIVE_LONG);
	}

	private void registerIntType(String typeName, DType intType, EffectiveShape effectiveShape) {
		DTypeImpl type = new DTypeImpl(Shape.INTEGER, null, typeName, intType);
		registerType(typeName, type);
		type.setEffectiveShape(effectiveShape);
	}

	public void registerDateAndTimeOnly() {
		DType intType = registry.getType(BuiltInTypes.DATE_SHAPE);

		registerDateType(BuiltInDateOrTimeOnlyTypes.DATE_DATE_ONLY.name(), intType, EffectiveShape.EFFECTIVE_DATE_ONLY);
		registerDateType(BuiltInDateOrTimeOnlyTypes.DATE_TIME_ONLY.name(), intType, EffectiveShape.EFFECTIVE_TIME_ONLY);
	}
	private void registerDateType(String typeName, DType intType, EffectiveShape effectiveShape) {
		DTypeImpl type = new DTypeImpl(Shape.DATE, null, typeName, intType);
		registerType(typeName, type);
		type.setEffectiveShape(effectiveShape);
	}

}