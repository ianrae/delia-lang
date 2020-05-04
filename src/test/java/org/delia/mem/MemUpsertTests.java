package org.delia.mem;

import static org.junit.Assert.assertEquals;

import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.sort.topo.TopoTestBase;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class MemUpsertTests extends TopoTestBase {
	
	@Test(expected=DeliaException.class)
	public void testSerial() {
		createCustomerTypeWithSerial();
		
		execStatement("upsert Customer {wid:44}");
		ResultValue res = this.execStatement("let x = Customer[true]");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(1, dval.asStruct().getField("id").asInt());
		assertEquals(44, dval.asStruct().getField("wid").asInt());
	}
	
	@Test
	public void testPrimaryKey() {
		createCustomer();
		//this is the normal way. use primary key filter
		ResultValue res = execStatement("upsert Customer[55] {wid:44}");
		assertEquals(true, res.ok);
		assertEquals(1, res.val); 

		res = this.execStatement("let x = Customer[true]");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(55, dval.asStruct().getField("id").asInt());
		assertEquals(44, dval.asStruct().getField("wid").asInt());
	}
	
	@Test(expected=DeliaException.class)
	public void testOtherUniqueFilter() {
		createCustomer();
		//alternative way. put primary key in fields and then can use any filter
		//that matches 0 or 1 records
		ResultValue res = execStatement("upsert Customer[wid==19] {id:55, wid:44}");
		assertEquals(true, res.ok);
		assertEquals(1, res.val); 
		
		res = this.execStatement("let x = Customer[true]");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(55, dval.asStruct().getField("id").asInt());
		assertEquals(44, dval.asStruct().getField("wid").asInt());
	}
	
	@Test
	public void testUpdate() {
		createCustomer();
		execStatement("insert Customer {id:55, wid:44}");
		execStatement("insert Customer {id:56, wid:44}");
		
		ResultValue res = execStatement("upsert Customer[55] {wid:444}");
		assertEquals(true, res.ok);
		assertEquals(1, res.val); //1 means we updated
		
		res = this.execStatement("let x = Customer[55]");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(55, dval.asStruct().getField("id").asInt());
		assertEquals(444, dval.asStruct().getField("wid").asInt());
	}
	
	
	@Test(expected=DeliaException.class)
	public void testNonUniqueFilter() {
		createCustomer();
		execStatement("insert Customer {id:55, wid:44}");
		execStatement("insert Customer {id:56, wid:44}");
		
		execStatement("upsert Customer[wid==44] {id:55, wid:44}");
		ResultValue res = this.execStatement("let x = Customer[true]");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(55, dval.asStruct().getField("id").asInt());
		assertEquals(44, dval.asStruct().getField("wid").asInt());
	}
	
	// --
	
	@Before
	public void init() {
		super.init();
		enableAutoCreateTables();
	}
	
	private String createCustomerType(boolean withSerial) {
		String s = withSerial ? "serial" : "";
		String src = String.format("type %s struct { id int primaryKey %s, wid int} end", "Customer", s);
		src += "\n";
		return src;
	}
	
	private void createCustomer() {
		String src = createCustomerType(false);
		execTypeStatement(src);
		DTypeRegistry registry = sess.getExecutionContext().registry;
		DStructType dtype = (DStructType) registry.getType("Customer");
		assertEquals(false, dtype.fieldIsOptional("id"));
		assertEquals(true, dtype.fieldIsPrimaryKey("id"));
		assertEquals(false, dtype.fieldIsSerial("id"));
	}
	private void createCustomerTypeWithSerial() {
		String src = createCustomerType(true);
		execTypeStatement(src);
		DTypeRegistry registry = sess.getExecutionContext().registry;
		DStructType dtype = (DStructType) registry.getType("Customer");
		assertEquals(false, dtype.fieldIsOptional("id"));
		assertEquals(true, dtype.fieldIsPrimaryKey("id"));
		assertEquals(true, dtype.fieldIsSerial("id"));
	}
	
}
