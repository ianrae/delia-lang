package org.delia.relation.named;

import static org.junit.Assert.assertEquals;

import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class MultipleRelationTests extends MultipleRelationTestBase {
	
	@Test
	public void test() {
		createCustomer11TypeWithRelations();
		RelationOneRule rr = getOneRule("Address", "cust1");
		chkRule(rr, true, "addr1", "addr1");

		rr = getOneRule("Customer", "addr1");
		chkRule(rr, true, "addr1", "addr1");

		rr = getOneRule("Customer", "addr2");
		chkRule(rr, true, "addr2", "addr2");
		
		ResultValue res = delia.continueExecution("let x = 5", this.sess);
		DValue dval = res.getAsDValue();
		assertEquals(5, dval.asInt());
		
		res = delia.continueExecution("insert Customer { wid:10 }", this.sess);
		dval = res.getAsDValue();
		assertEquals(null, dval);
	}
	
	@Test
	public void test2() {
		createCustomer11TypeWithRelations();
		doInsert("insert Customer { wid:11 }");
		doInsert("insert Customer { wid:12 }");
		doInsert("insert Address { z:20, cust1:1, cust2:2 }");
		
		DValue dvalA = doQuery("Address[1]");
		chkRelation(dvalA, "cust1", 1);
		chkRelation(dvalA, "cust2", 2);
		
		DValue dval = doQuery("Address[1].cust1.id");
		assertEquals(1, dval.asInt());
		dval = doQuery("Address[1].cust2.id");
		assertEquals(2, dval.asInt());
		
		doInsert("insert Customer { wid:13 }");
		doInsert("insert Customer { wid:14 }");
		doInsert("insert Address { z:21, cust1:3, cust2:4 }");
		dvalA = doQuery("Address[2]");
		chkRelation(dvalA, "cust1", 3);
		chkRelation(dvalA, "cust2", 4);
		
		//for 1:1 parent we need fks() to get relations to get fks
		DValue dvalC = doQuery("Customer[1].fks()");
		dumpDVal(dvalC);
		chkRelation(dvalC, "addr1", 1);
		chkRelation(dvalC, "addr2", null);
		
		dvalC = doQuery("Customer[2].fks()");
		dumpDVal(dvalC);
		chkRelation(dvalC, "addr1", null);
		chkRelation(dvalC, "addr2", 1);
	}
	
	@Test
	public void test3() {
		createCustomer11TypeWithRelations();
		doInsert("insert Customer { wid:11 }");
		doInsert("insert Customer { wid:12 }");
		doInsert("insert Address { z:20, cust1:1, cust2:1 }");
		
		//for 1:1 parent we need fks() to get relations to get fks
		DValue dvalC = doQuery("Customer[1].fks()");
		dumpDVal(dvalC);
		chkRelation(dvalC, "addr1", 1); //is ok for 2 different relations to have same Customer
		chkRelation(dvalC, "addr2", 1);
	}
	
	@Test(expected=DeliaException.class)
	public void test4Fail() {
		createCustomer11TypeWithRelations();
		doInsert("insert Customer { wid:11 }");
		doInsert("insert Customer { wid:12 }");
		doInsert("insert Address { z:20, cust1:1, cust2:1 }");
		
		//for 1:1 parent we need fks() to get relations to get fks
		DValue dvalC = doQuery("Customer[1].fks()");
		dumpDVal(dvalC);
		chkRelation(dvalC, "addr1", 1);
		chkRelation(dvalC, "addr2", 1);
		
		doInsert("insert Customer { wid:13 }");
		doInsert("insert Customer { wid:14 }");
		doInsert("insert Address { z:20, cust1:1, cust2:1 }"); //should fail
		//ERROR: rule-relationOne: relation field 'cust1' one - foreign key '1' already used -- type Address - in rule: relationOne
	}
	
	@Test
	public void test5Update() {
		createCustomer11TypeWithRelations();
		doInsert("insert Customer { wid:11 }");
		doInsert("insert Customer { wid:12 }");
		doInsert("insert Address { z:20, cust1:1, cust2:1 }");
		doInsert("insert Customer { wid:13 }"); //3
		
		doUpdate("update Address[1] {cust1:3}");
		
		//for 1:1 parent we need fks() to get relations to get fks
		DValue dvalC = doQuery("Customer[1].fks()");
		dumpDVal(dvalC);
		chkRelation(dvalC, "addr1", null);
		chkRelation(dvalC, "addr2", 1);
		
		dvalC = doQuery("Customer[3].fks()");
		dumpDVal(dvalC);
		chkRelation(dvalC, "addr1", 1);
		chkRelation(dvalC, "addr2", null);
	}
	
	@Test
	public void test6Delete() {
		createCustomer11TypeWithRelations();
		doInsert("insert Customer { wid:11 }");
		doInsert("insert Customer { wid:12 }");
		doInsert("insert Address { z:20, cust1:1, cust2:1 }");
		doInsert("insert Customer { wid:13 }"); //3
		
		doDelete("delete Address[1]");
		
		//for 1:1 parent we need fks() to get relations to get fks
		DValue dvalC = doQuery("Customer[1].fks()");
		dumpDVal(dvalC);
		chkRelation(dvalC, "addr1", null);
		chkRelation(dvalC, "addr2", null);
	}

	//------------------------
	@Before
	public void init() {
		super.init();
	}

	private String create11CustomerType() {
		String src = String.format("type Customer struct { id int primaryKey serial, wid int, relation addr1 Address 'addr1' one optional,");
		src += String.format("\n relation addr2 Address 'addr2' one optional} end");
		src += "\n";
		src += String.format("\n type Address struct { id int primaryKey serial, z int, relation cust1 Customer 'addr1' one ");
		src += String.format("\n relation cust2 Customer 'addr2' one} end");
		src += "\n";
		return src;
	}

	private void createCustomer11TypeWithRelations() {
		String src = create11CustomerType();
		log.log(src);
		execTypeStatement(src);
	}

}
