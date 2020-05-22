package org.delia.parser;

import static org.junit.Assert.assertEquals;

import org.delia.base.FakeTypeCreator;
import org.delia.db.schema.SchemaFingerprintGenerator;
import org.delia.runner.TypeRegistryHelper;
import org.delia.type.DTypeRegistry;
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
		assertEquals("(v1)Customer:struct:{id:int:U/0,firstName:string:/0,lastName:string:O/0,points:int:O/0,flag:boolean:O/0}\n", fingerprint);
	}
	
	// --
	//private Runner runner;
	
	
}
