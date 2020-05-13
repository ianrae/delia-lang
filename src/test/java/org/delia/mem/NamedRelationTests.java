package org.delia.mem;

import static org.junit.Assert.assertEquals;

import java.util.function.Consumer;

import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.sort.topo.TopoTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class NamedRelationTests extends NamedRelationTestBase {
	
	/*
	 * TODO:
	 * test duplicate rule names
	 * test rule named same as other relation field
	 * get rid of DRuleHelper find other side fn
	 */
	
	
	@Test
	public void test0() {
		createCustomerTypeWithRelations("addr1", null, "addr1");
		
		execStatement("insert Customer {wid:44}");
		execStatement("insert Address {z:5, cust:1}");
		ResultValue res = this.execStatement("let x = Customer[true]");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(1, dval.asStruct().getField("id").asInt());
		assertEquals(44, dval.asStruct().getField("wid").asInt());
	}
	
	@Test
	public void test2UnNamed() {
		expectException("ambiguous-relation", (s) -> {
			createCustomerTypeWithRelations(null, null,null);
			this.execStatement("insert Customer {id:3, wid:44}");
		});
	}
	
	@Test
	public void testBadNameFail() {
		Consumer<String> fn = (parameter) -> { 
			createCustomerTypeWithRelations(null, null, "x");
		};
		expectException("ambiguous-relation", fn);
	}
	
	@Test
	public void testBadNameFail2() {
		//better syntax
		expectException("ambiguous-relation", (s) -> {
			createCustomerTypeWithRelations(null, null, "x");
		});
	}
	
	@Test
	public void testNameSameAsOtherField() {
		expectException("relation-names-must-be-unique", (s) -> {
			createCustomerTypeWithRelations(null, "addr1", "addr1");
		});
	}
	
	@Test
	public void test3() {
		expectException("named-relation-error", (s) -> {
			createCustomerTypeWithRelations("addr", "addr1", "addr1");
		});
	}
	
	@Test
	public void testDup() {
		expectException("named-relation-error", (s) -> {
			createCustomerTypeWithRelations("addr2", "addr2", "addr1");
		});
	}
	
	@Test
	public void testUnknown() {
		expectException("named-relation-error", (s) -> {
			createCustomerTypeWithRelations("addr1", null, "x");
		});
	}
	
	// --
	
	@Before
	public void init() {
		super.init();
		enableAutoCreateTables();
	}
	
	private String createCustomerType(String relationName1, String relationName2, String relationName3) {
		String rn1 = relationName1 == null ? "" : String.format("'%s'", relationName1);
		String rn2 = relationName2 == null ? "" : String.format("'%s'", relationName2);
		String rn3 = relationName3 == null ? "" : String.format("'%s'", relationName3);
		
		String src = String.format("type Customer struct { id int primaryKey serial, wid int, relation addr1 Address %s one optional,", rn1);
		src += String.format(" relation addr2 Address %s one optional} end", rn2);
		src += "\n";
		src += String.format("\n type Address struct { id int primaryKey serial, z int, relation cust Customer %s one} end",rn3);
		src += "\n";
		return src;
	}
	
	private void createCustomerTypeWithRelations(String relationName1, String relationName2, String relationName3) {
		String src = createCustomerType(relationName1, relationName2, relationName3);
		log.log(src);
		execTypeStatement(src);
	}
	
}
