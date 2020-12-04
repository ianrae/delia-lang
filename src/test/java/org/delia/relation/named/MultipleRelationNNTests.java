package org.delia.relation.named;

import static org.junit.Assert.assertEquals;

import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class MultipleRelationNNTests extends MultipleRelationTestBase {
	
	@Test
	public void test() {
		createCustomerNNTypeWithRelations();
		RelationManyRule rr = getManyRule("Address", "cust1");
		chkRule(rr, true, "r1", "r1");

		RelationManyRule rr2 = getManyRule("Customer", "addr1");
		chkRule(rr2, true, "r1", "r1");

		rr = getManyRule("Customer", "addr2");
		chkRule(rr, true, "r2", "r2");
		
		ResultValue res = delia.continueExecution("let x = 5", this.sess);
		DValue dval = res.getAsDValue();
		assertEquals(5, dval.asInt());
		
		res = delia.continueExecution("insert Customer { wid:10 }", this.sess);
		dval = res.getAsDValue();
		assertEquals(null, dval);
	}
	
	@Test
	public void test2() {
		createCustomerNNTypeWithRelations();
		doInsert("insert Customer { wid:11 }");
		doInsert("insert Customer { wid:12 }");
		doInsert("insert Address { z:20, cust1:1, cust2:2 }");
		doInsert("insert Address { z:21, cust1:1 }");
		
		DValue dvalA = doQuery("Address[1]");
		//Note. ManyToMany relations in SQL always fill in fks. Kind of a quirk of the language. Perhaps fix later
//		chkRelation(dvalA, "cust1", null);
//		chkRelation(dvalA, "cust2", null);
		chkRelation(dvalA, "cust1", 1);
		chkRelation(dvalA, "cust2", 2);
		
//		DValue dval = doQuery("Address[1].cust1.id");
//		assertEquals(1, dval.asInt());
//		dval = doQuery("Address[1].cust2.id");
//		assertEquals(2, dval.asInt());
		
		doInsert("insert Customer { wid:13 }");
		doInsert("insert Customer { wid:14 }");
		doInsert("insert Address { z:21, cust1:3, cust2:4 }");
		dvalA = doQuery("Address[3]");
		chkRelation(dvalA, "cust1", 3);
		chkRelation(dvalA, "cust2", 4);
		
		//for 1:1 parent we need fks() to get relations to get fks
		DValue dvalC = doQuery("Customer[1].fks()");
		dumpDVal(dvalC);
		this.chkManyRelation(dvalC, "addr1", 1, 2);
		chkRelation(dvalC, "addr2", null);
		
		dvalC = doQuery("Customer[2].fks()");
		dumpDVal(dvalC);
		chkRelation(dvalC, "addr1", null);
		chkRelation(dvalC, "addr2", 1);
	}
	
	@Test
	public void test3() {
		createCustomerNNTypeWithRelations();
		doInsert("insert Customer { wid:11 }");
		doInsert("insert Customer { wid:12 }");
		doInsert("insert Address { z:20, cust1:1, cust2:1 }");
		
		//for 1:1 parent we need fks() to get relations to get fks
		DValue dvalC = doQuery("Customer[1].fks()");
		dumpDVal(dvalC);
		chkRelation(dvalC, "addr1", 1); //is ok for 2 different relations to have same Customer
		chkRelation(dvalC, "addr2", 1);
	}
	
	@Test
	public void test4Fail() {
		createCustomerNNTypeWithRelations();
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
		doInsert("insert Address { z:20, cust1:1, cust2:1 }"); //should not fail
	}
	
	@Test
	public void test5Update() {
		createCustomerNNTypeWithRelations();
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
		createCustomerNNTypeWithRelations();
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
	
	protected void chkManyRelation(DValue dvalA, String fieldName, Integer... ids) {
		DValue inner = dvalA.asStruct().getField(fieldName);
		if (ids.length == 0) {
			assertEquals(null, inner);
		} else {
			DRelation drel = inner.asRelation();
			assertEquals(ids.length, drel.getMultipleKeys().size());
			int i = 0;
			for(DValue dval: drel.getMultipleKeys()) {
				assertEquals(ids[i].intValue(), dval.asInt());
				i++;
			}
		}
	}
	

	private String createCustomerType() {
		String src = String.format("type Customer struct { id int primaryKey serial, wid int,");
		src += String.format("\n relation addr1 Address 'r1' many optional,");
		src += String.format("\n relation addr2 Address 'r2' many optional } end");
		src += "\n";
		src += String.format("\n type Address struct { id int primaryKey serial, z int, relation cust1 Customer 'r1' many ");
		src += String.format("\n relation cust2 Customer 'r2' many optional} end");
		src += "\n";
		return src;
	}

	private void createCustomerNNTypeWithRelations() {
		String src = createCustomerType();
		log.log(src);
		execTypeStatement(src);
	}

}
