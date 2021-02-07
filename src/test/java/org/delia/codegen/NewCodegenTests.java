package org.delia.codegen;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.delia.api.DeliaSession;
import org.delia.db.sizeof.DeliaTestBase;
import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * rules are built in RuleFuncFactory
 * @author Ian Rae
 *
 */
public class NewCodegenTests extends DeliaTestBase { 
	
	public static class CodeGeneratorOptions {
		public boolean addJsonIgnoreToRelations;
	}
	
	public interface CodeGenerator {
		 void setOptions(CodeGeneratorOptions options);
		 void setRegistry(DTypeRegistry registry);
		 void setPackageName(String packageName);
	     boolean canProcess(DType dtype);
	     String buildJavaFileName(DType dtype);
	     String generate(DType dtype);
	}
	
	public static class CodeGeneratorService {

		private List<String> typeNameL;
		private List<CodeGenerator> generatorL;
		private String packageName;
		private DTypeRegistry registry;
		private File outputDir;
		private StringBuilder sb;
		private CodeGeneratorOptions options = new CodeGeneratorOptions();
		
		public CodeGeneratorService(DTypeRegistry registry, List<String> typeNames, List<CodeGenerator> generatorsL, String packageName) {
			this.registry = registry;
			this.typeNameL = typeNames;
			this.generatorL = generatorsL;
			this.packageName = packageName;
		}

		public boolean run(File dir) {
			this.outputDir = dir;
			return doRun(true);
		}
		public boolean run(StringBuilder sb) {
			this.sb = sb;
			return doRun(false);
		}
		
		protected boolean doRun(boolean outputToFile) {
			
			for(String typeName: typeNameL) {
				DType dtype = registry.getType(typeName);
				
				for(CodeGenerator gen: generatorL) {
					gen.setRegistry(registry);
					gen.setOptions(options);
					gen.setPackageName(typeName);
					if (!gen.canProcess(dtype)) {
						continue;
					}
					
					String fileName = gen.buildJavaFileName(dtype);
					String text = gen.generate(dtype);
					
					if (outputToFile) {
						writeToFile(fileName, text);
					} else {
						sb.append(text);
						sb.append(StringUtil.eol());
					}
				}
				
			}
			return true;
		}

		private void writeToFile(String fileName, String text) {
			// TODO Auto-generated method stub
			
		}

		public CodeGeneratorOptions getOptions() {
			return options;
		}

		public void setOptions(CodeGeneratorOptions options) {
			this.options = options;
		}
	}
	
	public static abstract class NewCodeGenBase implements CodeGenerator {
		
		protected CodeGeneratorOptions options;
		protected String packageName;
		protected boolean structTypesOnly;
		private DTypeRegistry registry;
		private CodeGenHelper helper;

		public NewCodeGenBase(boolean structTypesOnly) {
			this.structTypesOnly = structTypesOnly;
			this.helper = null;//new CodeGenHelper(DTypeRegistry registry, String packageName) {

		}
		
		protected CodeGenHelper helper() {
			if (this.helper == null) {
				helper = new CodeGenHelper(registry, packageName);
			}
			return helper;
		}

		@Override
		public void setOptions(CodeGeneratorOptions options) {
			this.options = options;
		}

		@Override
		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}

		@Override
		public boolean canProcess(DType dtype) {
			if (structTypesOnly) {
				return dtype.isStructShape();
			}
			return false;
		}

		@Override
		public String buildJavaFileName(DType dtype) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setRegistry(DTypeRegistry registry) {
			this.registry = registry;
		}
	}
	
	public static class GetterInterfaceCodeGen extends NewCodeGenBase {

		public GetterInterfaceCodeGen() {
			super(true);
		}

		@Override
		public String generate(DType typeParam) {
			DStructType structType = (DStructType) typeParam; //structTypesOnly is true
			String typeName = structType.getName();

			StrCreator sc = new StrCreator();
			helper().addImports(sc, structType);
			sc.o("import org.delia.codegen.DeliaImmutable;");
			sc.nl();
			sc.nl();

			String baseType = (structType.getBaseType() == null) ? "DeliaImmutable" : structType.getBaseType().getName();
			sc.o("public interface %s extends %s {", typeName, baseType);
			sc.nl();
			for(String fieldName: structType.getDeclaredFields().keySet()) {
				DType ftype = structType.getDeclaredFields().get(fieldName);

				String javaType = helper().convertToJava(structType, fieldName);
				boolean hasPK = helper().hasPK(ftype);
				if (options.addJsonIgnoreToRelations && hasPK) {
					sc.o("  @JsonIgnore");
					sc.nl();
				}
				sc.o("  %s get%s();", javaType, StringUtil.uppify(fieldName));
				sc.nl();
				
				if (hasPK) {
					String pkType = helper().getPKType(ftype);
					sc.o("  %s get%sPK();", pkType, StringUtil.uppify(fieldName));
					sc.nl();
				}

			}
			sc.o("}");
			sc.nl();

			return sc.toString();
		}


		protected List<String> getImportList(DStructType structType) {
			List<String> list = helper().getImportList(structType);
			if (options.addJsonIgnoreToRelations) {
				list.add("import com.fasterxml.jackson.annotation.JsonIgnore;");
			}
			return list;
		}

	}
	
	public static class CGBuilder4 {
		CGBuilder3 builder3;

		public CGBuilder4(CGBuilder3 builder3) {
			this.builder3 = builder3;
		}
		
		public CodeGeneratorService build() {
			List<String> typeNames = buildTypeNamesList();
			CodegenBuilder builder = builder3.builder2.builder;
			return new CodeGeneratorService(builder.registry, typeNames, builder3.builder2.generatorsL, builder3.packageName);
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
			generatorsL.add(new GetterInterfaceCodeGen());
			//TODO more
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
		log.log("==== output ====");
		log.log(sb.toString());
		assertEquals(true, b2);
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
