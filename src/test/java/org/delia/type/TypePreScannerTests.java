package org.delia.type;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.delia.api.Delia;
import org.delia.base.DBTestHelper;
import org.delia.bdd.BDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.DeliaCompiler;
import org.delia.compiler.ast.Exp;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.error.DeliaError;
import org.delia.typebuilder.PreTypeRegistry;
import org.delia.typebuilder.TypePreRunner;
import org.delia.zdb.ZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;


public class TypePreScannerTests extends BDDBase {
	
	@Test
	public void test() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);
		
		DTypeRegistry registry = dao.getRegistry();
		assertEquals(DTypeRegistry.NUM_BUILTIN_TYPES + 2, registry.size());

		Set<String> set = registry.getAllCustomTypes();
		assertEquals(2, set.size());
		assertEquals(true, set.contains("Customer"));
	}

	@Test
	public void test2() {
		String src = buildSrc();
		Delia delia = createDelia();
		
		List<DeliaError> allErrors = new ArrayList<>();
		TypePreRunner preRunner = runPreTypeRunner(src, allErrors, delia);
		assertEquals(0, allErrors.size());
		PreTypeRegistry preReg = preRunner.getPreRegistry();
		assertEquals(2, preReg.size());
		assertEquals(0, preReg.getUndefinedTypes().size());
		
		for(String typeName: preReg.getMap().keySet()) {
			DType dtype = preReg.getMap().get(typeName).dtype;
			log.log("%s: %s", dtype.getShape().name(), typeName);
		}
		
		DTypeRegistry registry = preRunner.getActualRegistry();
		assertEquals(DTypeRegistry.NUM_BUILTIN_TYPES + 0, registry.size());
	}
	
	@Test
	public void testBad() {
		String src = buildBadSrc();
		Delia delia = createDelia();
		
		List<DeliaError> allErrors = new ArrayList<>();
		TypePreRunner preRunner = runPreTypeRunner(src, allErrors, delia);
		assertEquals(1, allErrors.size());
		PreTypeRegistry preReg = preRunner.getPreRegistry();
		assertEquals(3, preReg.size());
		assertEquals(1, preReg.getUndefinedTypes().size());
		assertEquals("XCustomer", preReg.getUndefinedTypes().get(0));
		
		for(String typeName: preReg.getMap().keySet()) {
			DType dtype = preReg.getMap().get(typeName).dtype;
			log.log("%s: %s", dtype.getShape().name(), typeName);
		}
		
		DTypeRegistry registry = preRunner.getActualRegistry();
		assertEquals(DTypeRegistry.NUM_BUILTIN_TYPES + 0, registry.size());
	}
	
	private TypePreRunner runPreTypeRunner(String src, List<DeliaError> allErrors, Delia delia) {
		DeliaCompiler compiler = delia.createCompiler();
		compiler.setDoPass3Flag(false); //so compiler doesn't catch unknown type CustomerX
		List<Exp> expL = compiler.parse(src);
		
		DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
		registryBuilder.init();
		DTypeRegistry registry = registryBuilder.getRegistry();
		
		TypePreRunner preRunner = new TypePreRunner(delia.getFactoryService(), registry);
		preRunner.executeStatements(expL, allErrors);
		return preRunner;
	}

	//---

	@Before
	public void init() {
	}

	private DeliaGenericDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaGenericDao(delia);
	}

	private String buildSrc() {
		String src = "type Customer struct {id int primaryKey, x int, relation addr Address one optional} end";
		src += "\n type Address struct {sid string primaryKey, y int, relation cust Customer one} end";
		return src;
	}
	private String buildBadSrc() {
		String src = "type Customer struct {id int primaryKey, x int, relation addr Address one optional} end";
		src += "\n type Address struct {sid string primaryKey, y int, relation cust XCustomer one} end";
		return src;
	}
	
	private Delia createDelia() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);
		
		Delia delia = dao.getDelia();
		return delia;
	}
	

	@Override
	public ZDBInterfaceFactory createForTest() {
		ZDBInterfaceFactory db = DBTestHelper.createMEMDb(createFactorySvc());
		return db;
	}

}
