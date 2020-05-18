package org.delia.relation.named;

import static org.junit.Assert.assertEquals;

import org.delia.relation.RelationInfo;
import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.ResultValue;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class SingleNamedRelationTests extends NamedRelationTestBase {
	
	@FunctionalInterface
	public interface ManyToManyVisitor {
		void visit(DStructType structType, RelationInfo relinfo);
	}
	@FunctionalInterface
	public interface ManyToManyValueVisitor {
		void visit(DValue structVal, DStructType structType, RelationInfo relinfo, DRelation drel);
	}
	
	private void foo(DStructType structType, ManyToManyVisitor fn) {
		for(DRule rule: structType.getRawRules()) {
			if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
				fn.visit(structType, rr.relInfo);
			}
		}
	}
	private void foox(DValue structVal, ManyToManyValueVisitor fn) {
		if (! structVal.getType().isStructShape()) {
			return;
		}
		DStructType structType = (DStructType) structVal.getType();
		for(DRule rule: structVal.getType().getRawRules()) {
			if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
				DValue inner = structVal.asStruct().getField(rr.relInfo.fieldName);
				DRelation drel = inner == null ? null : inner.asRelation();
				fn.visit(structVal, structType, rr.relInfo, drel);
			}
		}

	}
	
	@Test
	public void test0() {
		createCustomerTypeWithRelations("addr1", "addr1");
		
		execStatement("insert Customer {wid:44}");
		execStatement(
				"insert Address {z:5, cust:1}");
		ResultValue res = this.execStatement("let x = Customer[true]");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(1, dval.asStruct().getField("id").asInt());
		assertEquals(44, dval.asStruct().getField("wid").asInt());
		
		RelationOneRule rr = getOneRule("Address", "cust"); 
		chkRule(rr, true, "addr1", "addr1");
		
		rr = getOneRule("Customer", "addr1"); 
		chkRule(rr, true, "addr1", "addr1");
	}
	
	@Test
	public void test2() {
		createCustomerTypeWithRelations("wid", null);
		RelationOneRule rr = getOneRule("Address", "cust"); 
		chkRule(rr, false, "cust", "wid");

		rr = getOneRule("Customer", "addr1"); 
		chkRule(rr, true, "wid", "cust");
	}

	@Test
	public void testOK() {
		createCustomerTypeWithRelations(null, null);
		createCustomerTypeWithRelations(null, "joe");
		createCustomerTypeWithRelations("joe", null);
		createCustomerTypeWithRelations("joe", "joe");
	}


	// --
	
	@Before
	public void init() {
		super.init();
		enableAutoCreateTables();
	}
	
	private String createCustomerType(String relationName1, String relationName3) {
		String rn1 = relationName1 == null ? "" : String.format("'%s'", relationName1);
		String rn3 = relationName3 == null ? "" : String.format("'%s'", relationName3);
		
		String src = String.format("type Customer struct { id int primaryKey serial, wid int, relation addr1 Address %s one optional } end", rn1);
		src += "\n";
		src += String.format("\n type Address struct { id int primaryKey serial, z int, relation cust Customer %s one} end",rn3);
		src += "\n";
		return src;
	}
	
	private void createCustomerTypeWithRelations(String relationName1, String relationName3) {
		String src = createCustomerType(relationName1, relationName3);
		log.log(src);
		execTypeStatement(src);
	}
	
}
