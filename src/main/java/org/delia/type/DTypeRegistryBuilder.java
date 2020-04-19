package org.delia.type;

public class DTypeRegistryBuilder {
	private DTypeRegistry registry = new DTypeRegistry();

	public void init() {
		String name = BuiltInTypes.INTEGER_SHAPE.name();
		DType type = new DType(Shape.INTEGER, name, null);
        registerType(name, type);

        name = BuiltInTypes.LONG_SHAPE.name();
        type = new DType(Shape.LONG, name, null);
        registerType(name, type);

        name = BuiltInTypes.NUMBER_SHAPE.name();
        type = new DType(Shape.NUMBER, name, null);
        registerType(name, type);

		name = BuiltInTypes.STRING_SHAPE.name();
		type = new DType(Shape.STRING, name, null);
        registerType(name, type);

		name = BuiltInTypes.BOOLEAN_SHAPE.name();
		type = new DType(Shape.BOOLEAN, name, null);
        registerType(name, type);

		name = BuiltInTypes.DATE_SHAPE.name();
		type = new DType(Shape.DATE, name, null);
        registerType(name, type);
        
        name = BuiltInTypes.RELATION_SHAPE.name();
        type = new DType(Shape.RELATION, name, null);
        registerType(name, type);
	}
	
    private void registerType(String typeName, DType dtype) {
        registry.add(typeName, dtype);
    }

	public DTypeRegistry getRegistry() {
		return registry;
	}
	
//	public void addFakeTypes() {
//		//TODO: remove this later
//		FakeTypeCreator creator = new FakeTypeCreator();
//		creator.createFakeTypes(registry);
//	}
}