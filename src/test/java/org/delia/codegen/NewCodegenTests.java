package org.delia.codegen;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.db.sizeof.DeliaTestBase;
import org.delia.type.BlobType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.WrappedBlob;
import org.delia.util.BlobUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * rules are built in RuleFuncFactory
 * @author Ian Rae
 *
 */
public class NewCodegenTests extends DeliaTestBase { 
	
	public static class CodeGeneratorService {

		public CodeGenerator(List<String> typeNames) {
			// TODO Auto-generated constructor stub
		}
		
	}
	
	public static class CGBuilder4 {
		CGBuilder3 builder3;

		public CGBuilder4(CGBuilder3 builder3) {
			this.builder3 = builder3;
		}
		
		public CodeGeneratorService build() {
			List<String> typeNames = buildTypeNamesList();
			return new CodeGeneratorService(typeNames);
		}

		private List<String> buildTypeNamesList() {
			CodegenBuilder builder = builder3.builder2.builder;
			if (builder.allTypes) {
				return builder.registry.getOrderedList().stream().map(x -> x.getName()).collect(Collectors.toList());
			}
			return builder.theseTypes;
		}
		
	}
	public static class CGBuilder3 {
		CGBuilder2 builder2;

		public CGBuilder3(CGBuilder2 builder2) {
			this.builder2 = builder2;
		}
		
		public CGBuilder4 toPackage(String packageName) {
			return new CGBuilder4(this);
		}
		
	}
	public static class CGBuilder2 {
		CodegenBuilder builder;

		public CGBuilder2(CodegenBuilder builder) {
			this.builder = builder;
		}
		
		public CGBuilder3 addStandardGenerators() {
			return new CGBuilder3(this);
		}
		public CGBuilder3 addGenerator(Object generator) {
			return new CGBuilder3(this);
		}
		public CGBuilder3 addGenerators(List<Object> generators) {
			return new CGBuilder3(this);
		}
	}
	
	public static class CodegenBuilder {
		boolean allTypes;
		List<String> theseTypes;
		private DTypeRegistry registry;
		
		public static CodegenBuilder createBuilder(DeliaSession session) {
			CodegenBuilder builder = new CodegenBuilder(session.getRegistry());
			return builder;
		}
		
		public CodegenBuilder(DTypeRegistry registry) {
			this.registry = registry;
		}
		
		public CGBuilder2 allTypes() {
			allTypes = true;
			return new CGBuilder2(this);
		}
		public CGBuilder2 theseTypes(List<String> typeNames) {
			theseTypes = typeNames;
			return new CGBuilder2(this);
		}
	}
	
	@Test
	public void testDelia() {
		String src = "let x = Flight[1]";
		execute(src);
		DValue dval = session.getFinalResult().getAsDValue();
		assertEquals(1, dval.asStruct().getField("field1").asInt());
		WrappedBlob wblob = dval.asStruct().getField("field2").asBlob();
		assertEquals(BlobType.BTYE_ARRAY, wblob.type());
		String s = BlobUtils.toBase64(wblob.getByteArray());
		log.log(s);
		assertEquals("4E/QIA==", s);
		
		//check asString
		s = dval.asStruct().getField("field2").asString();
		assertEquals("4E/QIA==", s);
	}	
	
	
	@Before
	public void init() {
	}

	@Override
	protected String buildSrc() {
		String s = "";
		String src = String.format("type Flight struct {field1 int primaryKey, field2 blob } %s end", s);
		src += String.format("type Address struct {field1 int primaryKey, field2 string } %s end", s);

		s =  "";
		src += String.format("\n insert Flight {field1: 1, field2: '4E/QIA=='}");
		src += String.format("\n insert Flight {field1: 2, field2: '4E/QIA=='}");
		return src;
	}

}
