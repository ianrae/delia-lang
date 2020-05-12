package org.delia.sort.topo;

import org.delia.relation.RelationCardinality;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.junit.Before;
import org.junit.Test;

public class OneToManyTopoTests extends TopoTestBase {
	
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
		chkSorting("Customer,Address"); //parent first
		
		RelationManyRule rule = findManyRule("Customer");
		chkRelInfo(rule.relInfo, RelationCardinality.ONE_TO_MANY, "Customer", "addr", "Address", true, false);
		RelationOneRule xrule = findOneRule("Address");
		chkRelInfo(xrule.relInfo, RelationCardinality.ONE_TO_MANY, "Address", "cust", "Customer", false, false);
	}
	@Test
	public void test2() {
		createOneToMany("OOReverse");
		chkSorting("Customer,Address");
	}
	
	// --
	
	@Before
	public void init() {
		super.init();
	}
	
	private void createOneToMany(String mo) {
		String src = null;
		if (mo.equals("OO")) {
			src = createTypeSrc("Customer", "relation addr Address many optional");
			src += createTypeSrc("Address", "relation cust Customer one optional");
		} else if (mo.equals("OOReverse")) {
			src = createTypeSrc("Address", "relation cust Customer one optional");
			src += createTypeSrc("Customer", "relation addr Address many optional");
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
