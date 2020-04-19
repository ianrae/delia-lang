package org.delia.parser;

import static org.junit.Assert.assertEquals;

import org.delia.db.schema.SchemaFingerprintGenerator;
import org.delia.runner.TypeRegistryHelper;
import org.delia.type.DTypeRegistry;
import org.delia.typebuilder.FakeTypeCreator;
import org.junit.Test;


/**
 * @author Ian Rae
 *
 */
public class SchemaGeneratorTests {
	
	@Test
	public void test() {
		DTypeRegistry registry = TypeRegistryHelper.init();
		
		FakeTypeCreator creator = new FakeTypeCreator();
		creator.createFakeTypes(registry);
		assertEquals(true, registry.existsType("Customer"));
		
		
		SchemaFingerprintGenerator gen = new SchemaFingerprintGenerator();
		String fingerprint = gen.createFingerprint(registry);
		assertEquals("Customer:struct:{id:int:U,firstName:string:,lastName:string:O,points:int:O,flag:boolean:O}\n", fingerprint);
	}
	
	// --
	//private Runner runner;
	
	
}
