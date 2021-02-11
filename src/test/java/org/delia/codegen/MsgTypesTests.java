package org.delia.codegen;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.io.IOException;

import org.delia.DeliaLoader;
import org.delia.codegen.fluent.CodeGenBuilder;
import org.delia.db.sizeof.DeliaTestBase;
import org.junit.Before;
import org.junit.Test;

/**
 * lhub types codegen offers some new challenges
 * 
 * @author Ian Rae
 *
 */
public class MsgTypesTests extends DeliaTestBase { 
	
	@Test
	public void test() throws IOException {
		FileReader r = new FileReader("src/test/resources/test/msgTypes.txt");
		DeliaLoader loader = new DeliaLoader();
		String src = loader.fromReader(r);
		CodeGeneratorService codegen = CodeGenBuilder.create(src).allTypes().addStandardGenerators().toPackage("com.foo").build();
		codegen.getOptions().addJsonIgnoreToRelations = true;
		StringBuilder sb = new StringBuilder();
		boolean b2 = codegen.run(sb);
		log.log("==== output ====");
		log.log(sb.toString());
		assertEquals(true, b2);
	}	
	
	
	
	// --- helpers
	@Before
	public void init() {
	}

	@Override
	protected String buildSrc() {
		return "";
	}

}
