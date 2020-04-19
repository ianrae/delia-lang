package org.delia.runner;

import static org.junit.Assert.assertEquals;

import org.delia.type.DTypeRegistry;
import org.delia.typebuilder.FakeTypeCreator;
import org.junit.Test;


/**
 * Until we add type to Dang, we will create some fake types here
 * 
 * @author Ian Rae
 *
 */
public class FakeTypesTests {
	
	@Test
	public void test() {
		DTypeRegistry registry = TypeRegistryHelper.init();
		
		FakeTypeCreator creator = new FakeTypeCreator();
		creator.createFakeTypes(registry);
		assertEquals(true, registry.existsType("Customer"));
	}
	
	// --
	//private Runner runner;
	
	
}
