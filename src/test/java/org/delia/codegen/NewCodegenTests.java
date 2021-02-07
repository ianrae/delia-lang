package org.delia.codegen;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
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
	
	public interface CodeGenerator {
		
	}
	
	public static class CodeGeneratorService {

		public CodeGeneratorService(List<String> typeNames, List<CodeGenerator> generatorsL, String packageName) {
			// TODO Auto-generated constructor stub
		}

		public boolean run(File dir) {
			// TODO Auto-generated method stub
			return false;
		}
		public boolean run(StringBuilder sb) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
	public static class CGBuilder4 {
		CGBuilder3 builder3;

		public CGBuilder4(CGBuilder3 builder3) {
			this.builder3 = builder3;
		}
		
		public CodeGeneratorService build() {
			List<String> typeNames = buildTypeNamesList();
			return new CodeGeneratorService(typeNames, builder3.builder2.generatorsL, builder3.packageName);
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
		String packageName;

		public CGBuilder3(CGBuilder2 builder2) {
			this.builder2 = builder2;
		}
		
		public CGBuilder4 toPackage(String packageName) {
			this.packageName = packageName;
			return new CGBuilder4(this);
		}
		
	}
	public static class CGBuilder2 {
		CodegenBuilder builder;
		List<CodeGenerator> generatorsL = new ArrayList<>();
		
		public CGBuilder2(CodegenBuilder builder) {
			this.builder = builder;
		}
		
		public CGBuilder3 addStandardGenerators() {
			return new CGBuilder3(this);
		}
		public CGBuilder3 addGenerator(CodeGenerator generator) {
			generatorsL.add(generator);
			return new CGBuilder3(this);
		}
		public CGBuilder3 addGenerators(List<CodeGenerator> generators) {
			generatorsL.addAll(generators);
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
		
		CodeGeneratorService codegen = CodegenBuilder.createBuilder(session).allTypes().addStandardGenerators().toPackage("com.foo").build();
		
		String dir = "C:/tmp/delia/newcodegen";
		//boolean b = codegen.run(new File(dir));
		
		StringBuilder sb = new StringBuilder();
		boolean b2 = codegen.run(sb);
		log.log(sb.toString());
	}	
	
	
	@Before
	public void init() {
	}

	@Override
	protected String buildSrc() {
		String s = "";
		String src = String.format("type Flight struct {field1 int primaryKey, field2 blob } %s end", s);
		src += String.format(" type Address struct {id int primaryKey, name string } %s end", s);

		s =  "";
		src += String.format("\n insert Flight {field1: 1, field2: '4E/QIA=='}");
		src += String.format("\n insert Flight {field1: 2, field2: '4E/QIA=='}");
		return src;
	}

}
