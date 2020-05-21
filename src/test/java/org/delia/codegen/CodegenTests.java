package org.delia.codegen;


import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.app.DaoTestBase;
import org.delia.dao.DeliaDao;
import org.delia.runner.ResultValue;
import org.delia.type.DStructHelper;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;


public class CodegenTests extends DaoTestBase {

	@Test
	public void test2() {
		String src = buildSrc();
		DeliaDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		String typeName = "Flight";
		DTypeRegistry registry = dao.getMostRecentSession().getExecutionContext().registry;
		DStructType structType = (DStructType) registry.getType(typeName);
		GetterInterfaceCodeGen gen = new GetterInterfaceCodeGen(registry);
		String java = gen.generate(structType);
		log.log(java);
		log.log("////");
		ImmutCodeGen gen2 = new ImmutCodeGen(registry);
		java = gen2.generate(structType);
		log.log(java);
	}

	//---

	@Before
	public void init() {
	}

	private String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
}
