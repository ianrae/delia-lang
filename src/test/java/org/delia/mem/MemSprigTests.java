package org.delia.mem;

import static org.junit.Assert.assertEquals;

import org.delia.db.memdb.MemDBInterface;
import org.delia.db.memdb.SerialProvider;
import org.delia.runner.ResultValue;
import org.delia.sort.topo.TopoTestBase;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class MemSprigTests extends TopoTestBase {
	
	
	@Test
	public void test0() {
		createCustomerTypeWithSerial("int");
		
		execStatement("configure Customer.synthetic_id='sid'");
		execStatement("insert Customer {wid:44, sid:11}");
		ResultValue res = this.execStatement("let x = Customer[true]");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(1, dval.asStruct().getField("id").asInt());
		
		execStatement("insert Address {id:100, cust: 11}");
		res = this.execStatement("let y = Address[true]");
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertEquals(100, dval.asStruct().getField("id").asInt());
		DRelation drel = dval.asStruct().getField("cust").asRelation();
		assertEquals(1, drel.getForeignKey().asInt());
	}
	
	@Test
	public void test1Long() {
		createCustomerTypeWithSerial("long");
		
		SerialProvider.LongSerialGen.initialValue = new Long(55);
		execStatement("configure Customer.synthetic_id='sid'");
		//sprig ids are always int
		execStatement("insert Customer {wid:44, sid:11}");
		ResultValue res = this.execStatement("let x = Customer[true]");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(55L, dval.asStruct().getField("id").asLong());
		
		execStatement("insert Address {id:100, cust: 11}");
		res = this.execStatement("let y = Address[true]");
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertEquals(100, dval.asStruct().getField("id").asInt());
		DRelation drel = dval.asStruct().getField("cust").asRelation();
		assertEquals(55L, drel.getForeignKey().asLong());
	}
	@Test
	public void test1BigLong() {
		createCustomerTypeWithSerial("long");
		
		int max = Integer.MAX_VALUE;
		long bigId = Long.valueOf((long)max) + 10; //2147483647
		
		SerialProvider.LongSerialGen.initialValue = new Long(bigId);
		execStatement("configure Customer.synthetic_id='sid'");
		//sprig ids are always int
		execStatement("insert Customer {wid:44, sid:11}");
		ResultValue res = this.execStatement("let x = Customer[true]");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(bigId, dval.asStruct().getField("id").asLong());
		
		execStatement("insert Address {id:100, cust: 11}");
		res = this.execStatement("let y = Address[true]");
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertEquals(100, dval.asStruct().getField("id").asInt());
		DRelation drel = dval.asStruct().getField("cust").asRelation();
		assertEquals(bigId, drel.getForeignKey().asLong());
	}
	
//	@Test
//	public void testString() {
//		createCustomerTypeWithSerial("string");
//		
//		//string ids are still numeric value 
//		execStatement("configure Customer.synthetic_id='sid'");
//		//sprig ids are always int
//		execStatement("insert Customer {wid:44, sid:11}");
//		ResultValue res = this.execStatement("let x = Customer[true]");
//		assertEquals(true, res.ok);
//		DValue dval = res.getAsDValue();
//		assertEquals("1", dval.asStruct().getField("id").asString());
//		
//		execStatement("insert Address {id:100, cust: 11}");
//		res = this.execStatement("let y = Address[true]");
//		assertEquals(true, res.ok);
//		dval = res.getAsDValue();
//		assertEquals(100, dval.asStruct().getField("id").asInt());
//		DRelation drel = dval.asStruct().getField("cust").asRelation();
//		assertEquals("1", drel.getForeignKey().asString());
//	}
	
	
	// --
	
	@Before
	public void init() {
		super.init();
		enableAutoCreateTables();
	}
	
	private String createCustomerType(String idType) {
		String src = String.format("type %s struct { id %s primaryKey serial, wid int} end", "Customer", idType);
		src += "\n";
		src += createTypeSrc("Address", "relation cust Customer one optional");
		return src;
	}
	
	private void createCustomerTypeWithSerial(String idType) {
		String src = createCustomerType(idType);
		execTypeStatement(src);
		DTypeRegistry registry = sess.getExecutionContext().registry;
		DStructType dtype = (DStructType) registry.getType("Customer");
		assertEquals(false, dtype.fieldIsOptional("id"));
		assertEquals(true, dtype.fieldIsPrimaryKey("id"));
		assertEquals(true, dtype.fieldIsSerial("id"));
	}
	
}
