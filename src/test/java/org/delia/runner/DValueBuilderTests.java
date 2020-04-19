package org.delia.runner;

import static org.junit.Assert.assertEquals;

import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.typebuilder.FakeTypeCreator;
import org.delia.valuebuilder.BooleanValueBuilder;
import org.delia.valuebuilder.IntegerValueBuilder;
import org.delia.valuebuilder.RelationValueBuilder;
import org.delia.valuebuilder.StringValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;
import org.junit.Test;


/**
 * Until we add type to Dang, we will create some fake types here
 * 
 * @author Ian Rae
 *
 */
public class DValueBuilderTests {
	
	@Test
	public void testString() {
		DTypeRegistry registry = TypeRegistryHelper.init();
		DType type = TypeRegistryHelper.getStringType();
		StringValueBuilder builder = new StringValueBuilder(type);
		
		builder.buildFromString("bob");
		boolean b = builder.finish();
		assertEquals(true, b);
		DValue dval = builder.getDValue();
		
		assertEquals("bob", dval.asString());
	}
	@Test
	public void testInteger() {
		DTypeRegistry registry = TypeRegistryHelper.init();
		DType type = TypeRegistryHelper.getIntType();
		IntegerValueBuilder builder = new IntegerValueBuilder(type);
		
		builder.buildFromString("45");
		boolean b = builder.finish();
		assertEquals(true, b);
		DValue dval = builder.getDValue();
		
		assertEquals(45, dval.asInt());
	}
	@Test
	public void testBoolean() {
		DTypeRegistry registry = TypeRegistryHelper.init();
		DType type = TypeRegistryHelper.getBooleanType();
		BooleanValueBuilder builder = new BooleanValueBuilder(type);
		
		builder.buildFromString("true");
		boolean b = builder.finish();
		assertEquals(true, b);
		DValue dval = builder.getDValue();
		
		assertEquals(true, dval.asBoolean());
	}
	
	@Test
	public void testStruct() {
		DTypeRegistry registry = TypeRegistryHelper.init();
		FakeTypeCreator creator = new FakeTypeCreator();
		creator.createFakeTypes(registry);
		
		DType itype = TypeRegistryHelper.getIntType();
		DType stype = TypeRegistryHelper.getStringType();
		DType btype = TypeRegistryHelper.getBooleanType();
		DStructType type = (DStructType) registry.getType("Customer");
		StructValueBuilder structBuilder = new StructValueBuilder(type);
		
		IntegerValueBuilder ibuilder = new IntegerValueBuilder(itype);
		ibuilder.buildFromString("44");
		boolean b = ibuilder.finish();
		assertEquals(true, b);
		DValue dval = ibuilder.getDValue();
		structBuilder.addField("id", dval);
		
		StringValueBuilder builder = new StringValueBuilder(stype);
		builder.buildFromString("bob");
		b = builder.finish();
		assertEquals(true, b);
		dval = builder.getDValue();
		structBuilder.addField("firstName", dval);
		
		BooleanValueBuilder bbuilder = new BooleanValueBuilder(btype);
		bbuilder.buildFromString("true");
		b = bbuilder.finish();
		assertEquals(true, b);
		dval = bbuilder.getDValue();
		structBuilder.addField("flag", dval);
		
		b = structBuilder.finish();
		assertEquals(true, b);
		dval = structBuilder.getDValue();
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
		assertEquals(null, dval.asStruct().getField("lastName"));
		assertEquals(null, dval.asStruct().getField("points"));
		assertEquals(true, dval.asStruct().getField("flag").asBoolean());
	}
	
	@Test
	public void testRelation() {
		DTypeRegistry registry = TypeRegistryHelper.init();
		DType type = TypeRegistryHelper.getRelationType();
		FakeTypeCreator creator = new FakeTypeCreator();
		creator.createFakeTypes(registry);
		RelationValueBuilder builder = new RelationValueBuilder(type, "Customer", registry);
		
		builder.buildFromString("33");
		boolean b = builder.finish();
		assertEquals(true, b);
		DValue dval = builder.getDValue();
		DRelation drel = dval.asRelation();
		assertEquals(33, drel.getForeignKey().asInt());
	}
	
	// --
}
