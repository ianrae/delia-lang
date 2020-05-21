package org.delia.codegen;


import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.delia.app.DaoTestBase;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaGenericDao;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.util.TextFileWriter;
import org.junit.Before;
import org.junit.Test;


public class EntitySourceCodeGeneratorTests extends DaoTestBase {

	public static class EntitySourceCodeGenerator extends ServiceBase {

		public EntitySourceCodeGenerator(FactoryService factorySvc) {
			super(factorySvc);
		}
		
		public boolean createSourceFiles(DTypeRegistry registry, String packageName, String targetDir) {
			
			for(DType dtype: registry.getOrderedList()) {
				if (! dtype.isStructShape()) {
					continue;
				}
				
				DStructType structType = (DStructType) dtype;
				doGetterInteface(structType, registry, packageName, targetDir);
				doImmutClass(structType, registry, packageName, targetDir);
				doSetterInterface(structType, registry, packageName, targetDir);
				doEntityClass(structType, registry, packageName, targetDir);
			}
			
			return true;
		}

		private void doGetterInteface(DStructType structType, DTypeRegistry registry, String packageName, String targetDir) {
			GetterInterfaceCodeGen gen = new GetterInterfaceCodeGen(registry, packageName);
			String java = gen.generate(structType);
			
			String filename = String.format("%s.java", structType.getName());
			writeFile(targetDir, filename, java);
		}
		private void doImmutClass(DStructType structType, DTypeRegistry registry, String packageName,
				String targetDir) {
			ImmutCodeGen gen = new ImmutCodeGen(registry, packageName);
			String java = gen.generate(structType);
			
			String filename = String.format("%sImmut.java", structType.getName());
			writeFile(targetDir, filename, java);
		}
		private void doSetterInterface(DStructType structType, DTypeRegistry registry, String packageName,
				String targetDir) {
			SetterInterfaceCodeGen gen = new SetterInterfaceCodeGen(registry, packageName);
			String java = gen.generate(structType);
			
			String filename = String.format("%sSetter.java", structType.getName());
			writeFile(targetDir, filename, java);
		}
		private void doEntityClass(DStructType structType, DTypeRegistry registry, String packageName,
				String targetDir) {
			EntityCodeGen gen = new EntityCodeGen(registry, packageName);
			String java = gen.generate(structType);
			
			String filename = String.format("%sEntity.java", structType.getName());
			writeFile(targetDir, filename, java);
		}

		private void writeFile(String targetDir, String filename, String java) {
			TextFileWriter w = new TextFileWriter();
			String path = String.format("%s/%s", targetDir, filename); //TODO fix / and \
			w.writeFile(path, Collections.singletonList(java));
		}
	}
	
	
//	@Test
	public void test() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		DTypeRegistry registry = dao.getMostRecentSession().getExecutionContext().registry;
		EntitySourceCodeGenerator codegen = new EntitySourceCodeGenerator(dao.getFactorySvc());
		b = codegen.createSourceFiles(registry, "org.delia.codegen.sample", "C:/tmp/delia");
		assertEquals(true, b);
	}

	
	//---

	@Before
	public void init() {
	}

	private String buildSrc() {
		String src = "type Wing struct {id int, width int } end";
		src += "\n type Flight struct {field1 int unique, field2 int, dd date optional, relation wing Wing one optional } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
}
