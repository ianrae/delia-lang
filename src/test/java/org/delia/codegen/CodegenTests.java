//package org.delia.codegen;
//
//
//import static org.junit.Assert.assertEquals;
//
//import org.delia.app.DaoTestBase;
//import org.delia.dao.DeliaGenericDao;
//import org.delia.type.DStructType;
//import org.delia.type.DTypeRegistry;
//import org.junit.Before;
//import org.junit.Test;
//
//
//public class CodegenTests extends DaoTestBase {
//
//	@Test
//	public void test() {
//		String src = buildSrc();
//		DeliaGenericDao dao = createDao(); 
//		boolean b = dao.initialize(src);
//		assertEquals(true, b);
//
//		String typeName = "Flight";
//		String pkg = "a.b.argo";
//		DTypeRegistry registry = dao.getMostRecentSession().getExecutionContext().registry;
//		DStructType structType = (DStructType) registry.getType(typeName);
//		
//		GetterInterfaceCodeGen gen = new GetterInterfaceCodeGen(registry, pkg);
//		String java = gen.generate(structType);
//		log.log(java);
//		log.log("////");
//		ImmutCodeGen gen2 = new ImmutCodeGen(registry, pkg);
//		java = gen2.generate(structType);
//		log.log(java);
//	}
//
//	@Test
//	public void test2() {
//		String src = buildSrc();
//		DeliaGenericDao dao = createDao(); 
//		boolean b = dao.initialize(src);
//		assertEquals(true, b);
//
//		String typeName = "Flight";
//		String pkg = "a.b.argo";
//		DTypeRegistry registry = dao.getMostRecentSession().getExecutionContext().registry;
//		DStructType structType = (DStructType) registry.getType(typeName);
//		
//		EntityCodeGen gen = new EntityCodeGen(registry, pkg);
//		String java = gen.generate(structType);
//		log.log(java);
//	}
//
//	@Test
//	public void test3() {
//		String src = buildSrc();
//		DeliaGenericDao dao = createDao(); 
//		boolean b = dao.initialize(src);
//		assertEquals(true, b);
//
//		String typeName = "Flight";
//		String pkg = "a.b.argo";
//		DTypeRegistry registry = dao.getMostRecentSession().getExecutionContext().registry;
//		DStructType structType = (DStructType) registry.getType(typeName);
//		
//		SetterInterfaceCodeGen gen = new SetterInterfaceCodeGen(registry, pkg);
//		String java = gen.generate(structType);
//		log.log(java);
//	}
//	
//	//---
//
//	@Before
//	public void init() {
//	}
//
//	private String buildSrc() {
//		String src = "type Wing struct {id int, width int } end";
//		src += "\n type Flight struct {field1 int unique, field2 int, dd date optional, relation wing Wing one optional } end";
//		src += "\n insert Flight {field1: 1, field2: 10}";
//		src += "\n insert Flight {field1: 2, field2: 20}";
//		return src;
//	}
//}
