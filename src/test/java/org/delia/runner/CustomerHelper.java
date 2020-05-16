package org.delia.runner;

import static org.junit.Assert.assertEquals;

import org.delia.base.FakeTypeCreator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.BooleanValueBuilder;
import org.delia.valuebuilder.IntegerValueBuilder;
import org.delia.valuebuilder.StringValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;


public class CustomerHelper {
	
	// --
	public static DValue createCustomer() {
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
		return dval;
	}
}
