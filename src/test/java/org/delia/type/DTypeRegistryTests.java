package org.delia.type;


import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.delia.base.DBTestHelper;
import org.delia.bdd.BDDBase;
import org.delia.dao.DeliaGenericDao;
import org.delia.zdb.DBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;


public class DTypeRegistryTests extends BDDBase {
	
	
	@Test
	public void test2() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);
		
		DTypeRegistry registry = dao.getRegistry();
		assertEquals(DTypeRegistry.NUM_BUILTIN_TYPES + 1, registry.size());

		Set<String> set = registry.getAllCustomTypes();
		assertEquals(1, set.size());
		assertEquals(true, set.contains("Flight"));
	}

	
	//---

	@Before
	public void init() {
	}

	private DeliaGenericDao createDao() {
		return DBTestHelper.createDao();
	}

	private String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}

	@Override
	public DBInterfaceFactory createForTest() {
		DBInterfaceFactory db = DBTestHelper.createMEMDb(createFactorySvc());
		return db;
	}

}
