package org.delia.mem;

import static org.junit.Assert.*;

import org.delia.db.memdb.MemDBInterface;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.sort.topo.TopoTestBase;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class MemSerialTests extends TopoTestBase {
	
	
	@Test
	public void test0() {
		createCustomerTypeWithSerial();
		
		execStatement("insert Customer {wid:44}");
		ResultValue res = this.execStatement("let x = Customer[true]");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(1, dval.asStruct().getField("id").asInt());
		assertEquals(44, dval.asStruct().getField("wid").asInt());
	}
	
	@Test
	public void test1() {
		createCustomerTypeWithSerial();
		
		try {
			this.execStatement("insert Customer {id:3, wid:44}");
		} catch (DeliaException e) {
			assertEquals("serial-value-cannot-be-provided", e.getLastError().getId());
		}
	}
	
	// --
	
	@Before
	public void init() {
		super.init();
		enableAutoCreateTables();
	}
	
	private String createCustomerType() {
		String src = String.format("type %s struct { id int primaryKey serial, wid int} end", "Customer");
		src += "\n";
		return src;
	}
	
	private void createCustomerTypeWithSerial() {
		String src = createCustomerType();
		execTypeStatement(src);
		DTypeRegistry registry = sess.getExecutionContext().registry;
		DStructType dtype = (DStructType) registry.getType("Customer");
		assertEquals(false, dtype.fieldIsOptional("id"));
		assertEquals(true, dtype.fieldIsPrimaryKey("id"));
		assertEquals(true, dtype.fieldIsSerial("id"));
	}
	
}
