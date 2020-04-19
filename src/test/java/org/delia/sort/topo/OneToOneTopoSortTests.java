package org.delia.sort.topo;

import org.delia.relation.RelationCardinality;
import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.DeliaException;
import org.junit.Before;
import org.junit.Test;

public class OneToOneTopoSortTests extends TopoTestBase {
	
	//TYPE 2. 1:1 M-O Relation
//	  Customer  addr Address one optional
//	  Address   cust Customer one 
//
//	-parent mandatory, child optional (easy one)
//	-Customer is parent. Address is child
//	-FK in child
//	-MUST insert parent first w/o value for relation
	
	
	@Test
	public void test0() {
		chkOne("one optional parent", "one optional", true); //O-O
		chkSorting("Customer,Address"); //parent first
		
		RelationOneRule rule = findOneRule("Customer");
		chkRelInfo(rule.relInfo, RelationCardinality.ONE_TO_ONE, "Customer", "addr", "Address", true, false);
		rule = findOneRule("Address");
		chkRelInfo(rule.relInfo, RelationCardinality.ONE_TO_ONE, "Address", "cust", "Customer", false, false);
	}
	@Test
	public void test0a() {
		chkOne("one optional", "one optional parent", true); //O-O
		chkSorting("Address,Customer");//parent first
	}
	
	@Test(expected=DeliaException.class)
	public void testMissingParent() {
		chkOne("one optional", "one optional", true); //O-O
		chkSorting("Address,Customer"); 
	}
	
	
	@Test
	public void testOneWay() {
		createOneToOne("OneWayO");
		chkSorting("Customer,Address"); //parent first
		RelationOneRule rule = findOneRule("Address");
		chkRelInfo(rule.relInfo, RelationCardinality.ONE_TO_ONE, "Address", "cust", "Customer", false, true);
		
		createOneToOne("OneWayOReversed");
		chkSorting("Customer,Address"); //parent first
	}
	
	// --
	
	@Before
	public void init() {
		super.init();
	}
	
	private void createOneToOne(String mo) {
		String src = null;
		if (mo.equals("OO")) {
			src = createTypeSrc("Customer", "relation addr Address one optional");
			src += createTypeSrc("Address", "relation cust Customer one optional");
		} else if (mo.equals("MO")) {
			src = createTypeSrc("Customer", "relation addr Address one optional");
			src += createTypeSrc("Address", "relation cust Customer one");
		} else if (mo.equals("OM")) {
			src = createTypeSrc("Customer", "relation addr Address one");
			src += createTypeSrc("Address", "relation cust Customer one optional");
		} else if (mo.equals("OneWay")) {
			src = createTypeSrc("Customer", "");
			src += createTypeSrc("Address", "relation cust Customer one");
		} else if (mo.equals("OneWayO")) {
			src = createTypeSrc("Customer", "");
			src += createTypeSrc("Address", "relation cust Customer one optional");
		} else if (mo.equals("OneWayOReversed")) {
			src = createTypeSrc("Address", "relation cust Customer one optional");
			src += createTypeSrc("Customer", "");
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
