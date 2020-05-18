package org.delia.relation.named;

import static org.junit.Assert.*;

import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.delia.util.render.ObjectRendererImpl;
import org.junit.Before;
import org.junit.Test;

public class MultipleRelationTests extends NamedRelationTestBase {
	
	@Test
	public void test11() {
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
		doInsert("insert Customer { wid:11 }");
		
		doInsert("insert Address { z:20, cust1:1, cust2:2 }");
//		doInsert("insert Address { z:21 }");
		
		DValue dval2 = doQuery("Address[1]");
		
		dumpObj("payload..", dval2);

		
	}
	private DValue doQuery(String src) {
		ResultValue res = delia.continueExecution(src, this.sess);
		DValue dval = res.getAsDValue();
		return dval;
	}
	protected void dumpObj(String title, Object obj) {
		log(title);
		ObjectRendererImpl ori = new ObjectRendererImpl();
		String json = ori.render(obj);
		log(json);
	}

	private void doInsert(String src) {
		ResultValue res = delia.continueExecution(src, this.sess);
		DValue dval = res.getAsDValue();
		assertEquals(null, dval);
	}


	@Before
	public void init() {
		super.init();
		enableAutoCreateTables();
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
