package org.delia.scope.scopetest.relation;

import org.delia.db.memdb.MemDBInterface;
import org.delia.runner.ResultValue;
import org.junit.Before;
import org.junit.Test;

public class OneToManyRelationTests extends NewRelationTestBase {
	
//	TYPE 7. 1:N M-O Relation
//	  Customer  addr Address many optional
//	  Address   cost Customer one 
//
//	-'parent' not allowed
//	 -parent mandatory, child optional (easy one)
//	-Customer is parent. Address is child
//	-FK in child
//	-MUST insert parent first w/o value for relation
//	-then insert child with value for fk

	@Test
	public void test1() {
		chkOne("many optional", "one optional", true); //O-O
		chkOne("many optional", "one", true); //M-O
		chkOne("many", "one optional", true); //O-M
		chkOne("many", "one", false, "relation-mandatory-mandatory-not-allowed"); //M-M
		chkOne("many optional", "one parent", false);
	}
	
	@Test
	public void testOO() {
		createOneToMany("OO");
		
		execStatement("insert Customer { id:44 }");
		queryAndChkNull("Customer", 44, "addr");
		
		execStatement("insert Address { id:100, cust:44 }");
		queryAndChkCustomer(44);
		execStatement("insert Address { id:101, cust:44 }");
		queryAndChkCustomer(44);
		queryAddressFKAndChk(44, 2);

		ResultValue res = queryAndChk("Address", 100);
		chkRelation(res, "cust", 44, "Customer");
		res = queryAndChk("Address", 101);
		chkRelation(res, "cust", 44, "Customer");
	}
	@Test
	public void testOONull() {
		createOneToMany("OO");
		
		execStatement("insert Customer { id:44 }");
		queryAndChkNull("Customer", 44, "addr");
		
		execStatement("insert Address { id:100 }");
		queryAndChkCustomer(44);
		execStatement("insert Address { id:101, cust:44 }");
		queryAndChkCustomer(44);
		queryAddressFKAndChk(44, 1);

		ResultValue res = queryAndChk("Address", 100);
		chkRelationNull(res, "cust");
		queryAndChk("Customer", 44);
	}

	@Test
	public void testMO() {
		createOneToMany("MO");
		
		execStatement("insert Customer { id:44 }");
		queryAndChkNull("Customer", 44, "addr");
		
		execStatement("insert Address { id:100, cust:44 }");
		queryAndChkCustomer(44);
		execStatement("insert Address { id:101, cust:44 }");
		queryAndChkCustomer(44);

		ResultValue res = queryAndChk("Address", 100);
		chkRelation(res, "cust", 44, "Customer");
		queryAndChk("Customer", 44);
		queryAddressFKAndChk(44, 2);
	}
	@Test
	public void testMONull() {
		createOneToMany("MO");
		
		execStatement("insert Customer { id:44 }");
		queryAndChkNull("Customer", 44, "addr");

		execStatement("insert Customer { id:45 }");
		queryAndChkNull("Customer", 45, "addr");
		queryAddressFKAndChk(44, 0);
	}
	
	@Test
	public void testOM() {
		createOneToMany("OM");
		//TODO: this isn't really OM. i think we'll just put fk in Customer

		execStatement("configure loadFKs=true");
		execStatement("insert Address { id:100}");
		
		execStatement("insert Customer { id:44, addr:100 }");
		ResultValue res = queryAndChk("Customer", 44);
		chkRelation(res, "addr", 100, "Address");
//		queryAndChkNull("Customer", 44, "addr");

		//TODO: fix update execStatement("update Address[100] { cust:44 }");

		res = queryAndChk("Address", 100);
		chkRelation(res, "cust", 44, "Customer");
		queryAndChk("Customer", 44);
		queryAddressFKAndChk(44, 1);
	}
	
	
	//is no one-way N:1 relation
	
	// --
	
	@Before
	public void init() {
		super.init();
		enableAutoCreateTables();
	}
	
	private void createOneToMany(String mo) {
		String src = null;
		if (mo.equals("OO")) {
			src = createTypeSrc("Customer", "relation addr Address many optional");
			src += createTypeSrc("Address", "relation cust Customer one optional");
		} else if (mo.equals("MO")) {
			src = createTypeSrc("Customer", "relation addr Address many optional");
			src += createTypeSrc("Address", "relation cust Customer one");
		} else if (mo.equals("OM")) {
			src = createTypeSrc("Customer", "relation addr Address many");
			src += createTypeSrc("Address", "relation cust Customer one optional");
		} else if (mo.equals("OneWay")) {
			src = createTypeSrc("Customer", "");
			src += createTypeSrc("Address", "relation cust Customer one");
		} else if (mo.equals("OneWayO")) {
			src = createTypeSrc("Customer", "");
			src += createTypeSrc("Address", "relation cust Customer one optional");
		}
		execTypeStatement(src);
	}

	private void chkOne(String custModifiers, String addrModifiers, boolean allowed) {
		chkOne(custModifiers, addrModifiers, allowed, "relation-parent-not-allowed");
	}
	private void chkOne(String custModifiers, String addrModifiers, boolean allowed, String errId) {
		String src = createTypeSrc("Customer", String.format("relation addr Address %s", custModifiers));
		String s = String.format("relation cust Customer %s", addrModifiers);
		if (allowed) {
			String src2 = createTypeSrc("Address", s);
			execTypeStatement(src + " " + src2);
		} else {
			createTypeFail(src, "Address", s, errId);
		}
	}
}
