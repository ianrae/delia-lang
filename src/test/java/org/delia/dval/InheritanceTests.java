package org.delia.dval;


import static org.junit.Assert.assertEquals;

import org.delia.app.DaoTestBase;
import org.delia.dao.DeliaGenericDao;
import org.junit.Before;
import org.junit.Test;


public class InheritanceTests extends DaoTestBase {
	
	@Test
	public void test() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);
	}

	@Test
	public void testInFields() {
		String src = buildSrc2();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);
	}
	
	//---
	@Before
	public void init() {
	}

	private String buildSrc() {
		String src = "type Vehicle struct {field1 int unique, field2 int } end";
		src += "\n type Automobile Vehicle {numWheels int } end";
		src += "\n type Car Automobile { x int } end";
		src += "\n insert Vehicle {field1: 1, field2: 10 }";
		src += "\n insert Automobile {field1: 1, field2: 10, numWheels:4 }";
		src += "\n insert Car {field1: 1, field2: 10, numWheels:4, x: 100}";
		return src;
	}
	private String buildSrc2() {
		String src = "type Vehicle struct {field1 int unique, field2 int } end";
		src += "\n type Automobile Vehicle {numWheels int } end";
		src += "\n type Car Automobile { x int, relation aa Automobile one optional } end";
		src += "\n insert Vehicle {field1: 1, field2: 10 }";
		src += "\n insert Automobile {field1: 1, field2: 10, numWheels:4 }";
		src += "\n insert Car {field1: 1, field2: 10, numWheels:4, x: 100}";
		return src;
	}
	
}
