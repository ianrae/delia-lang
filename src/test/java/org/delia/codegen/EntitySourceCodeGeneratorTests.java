package org.delia.codegen;


import static org.junit.Assert.assertEquals;

import org.delia.app.DaoTestBase;
import org.delia.dao.DeliaGenericDao;
import org.delia.type.DTypeRegistry;
import org.junit.Before;
import org.junit.Test;


public class EntitySourceCodeGeneratorTests extends DaoTestBase {

		@Test
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
