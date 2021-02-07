package org.delia.codegen;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.delia.api.DeliaLoader;
import org.delia.codegen.fluent.CodeGenBuilder;
import org.delia.db.sizeof.DeliaTestBase;
import org.junit.Before;
import org.junit.Test;

/**
 * new version of codegen using a fluent api
 * 
 * @author Ian Rae
 *
 */
public class NewCodegenTests extends DeliaTestBase { 
	
	@Test
	public void testSimple() {
		String src = buildSrc();
		
		CodeGeneratorService codegen = CodeGenBuilder.create(src).allTypes().addStandardGenerators().toPackage("com.foo").build();
		codegen.getOptions().addJsonIgnoreToRelations = true;
		StringBuilder sb = new StringBuilder();
		boolean b2 = codegen.run(sb);
		log.log("==== output ====");
		log.log(sb.toString());
		assertEquals(true, b2);
	}	
	
	@Test
	public void testFileReader() throws IOException {
		FileReader r = new FileReader("src/test/resources/test/northwind/northwind-small.txt");
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
	
	@Test
	public void testResource() throws IOException {
		String resourcePath = "src/test/resources/test/northwind/northwind-small.txt";
		DeliaLoader loader = new DeliaLoader();
		String src = loader.fromResource(resourcePath);
		CodeGeneratorService codegen = CodeGenBuilder.create(src).allTypes().addStandardGenerators().toPackage("com.foo").build();
		codegen.getOptions().addJsonIgnoreToRelations = true;
		StringBuilder sb = new StringBuilder();
		boolean b2 = codegen.run(sb);
		log.log("==== output ====");
		log.log(sb.toString());
		assertEquals(true, b2);
	}	
	
	@Test
	public void test1() {
		String src = "let x = Flight[1]";
		execute(src);
		
		CodeGeneratorService codegen = CodeGenBuilder.create(session).allTypes().addStandardGenerators().toPackage("com.foo").build();
		codegen.getOptions().addJsonIgnoreToRelations = true;
		StringBuilder sb = new StringBuilder();
		boolean b2 = codegen.run(sb);
		log.log("==== output ====");
		log.log(sb.toString());
		assertEquals(true, b2);
	}	
	
	@Test
	public void testFile() {
		String src = "let x = Flight[1]";
		execute(src);
		
		CodeGeneratorService codegen = CodeGenBuilder.create(session).allTypes().addStandardGenerators().toPackage("com.foo").build();
		String dir = "C:/tmp/delia/newcodegen";
		boolean b = codegen.run(new File(dir));
		assertEquals(true, b);
	}	
	
	@Before
	public void init() {
	}

	@Override
	protected String buildSrc() {
		String s = "";
		String src = String.format("type Flight struct {field1 int primaryKey, field2 blob, relation  addr Address one parent optional } %s end", s);
		src += String.format(" type Address struct {id int primaryKey, name string, relation flight Flight one optional } %s end", s);

		s =  "";
		src += String.format("\n insert Flight {field1: 1, field2: '4E/QIA=='}");
		src += String.format("\n insert Flight {field1: 2, field2: '4E/QIA=='}");
		return src;
	}

}
