package org.delia.relation.named;

import static org.junit.Assert.*;

import java.util.List;

import org.delia.compiler.generate.DeliaGeneratePhase;
import org.delia.compiler.generate.SimpleFormatOutputGenerator;
import org.delia.log.LogLevel;
import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.ResultValue;
import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.delia.util.render.ObjectRendererImpl;
import org.junit.Before;
import org.junit.Test;

public class MultipleRelationTests extends NamedRelationTestBase {
	
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
	public void test3Fail() {
		createCustomer11TypeWithRelations();
		doInsert("insert Customer { wid:11 }");
		doInsert("insert Customer { wid:12 }");
		doInsert("insert Address { z:20, cust1:1, cust2:1 }");
		
		//for 1:1 parent we need fks() to get relations to get fks
		DValue dvalC = doQuery("Customer[1].fks()");
		dumpDVal(dvalC);
		chkRelation(dvalC, "addr1", 1);
		chkRelation(dvalC, "addr2", 1);
	}
	
	private void dumpDVal(DValue dvalC) {
		List<String> list = this.generateFromDVal(dvalC);
		for(String s: list) {
			log(s);
		}
	}

	//------------------------
	@Before
	public void init() {
		super.init();
		enableAutoCreateTables();
		this.log.setLevel(LogLevel.INFO);
	}

	private DValue chkRelation(DValue dvalA, String fieldName, Integer id) {
		DValue inner = dvalA.asStruct().getField(fieldName);
		if (id == null) {
			assertEquals(null, inner);
			return null;
		} else {
			DRelation drel = inner.asRelation();
			DValue dval2 = drel.getForeignKey();
			assertEquals(id.intValue(), dval2.asInt());
			return dval2;
		}
	}
	
	private DValue doQuery(String src) {
		log("src: " + src);
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
	private List<String> generateFromDVal(DValue dval) {
		SimpleFormatOutputGenerator gen = new SimpleFormatOutputGenerator();
		// ErrorTracker et = new SimpleErrorTracker(log);
		DeliaGeneratePhase phase = this.sess.getExecutionContext().generator;
		boolean b = phase.generateValue(gen, dval, "a");
		assertEquals(true, b);
		return gen.outputL;
	}

	private void doInsert(String src) {
		ResultValue res = delia.continueExecution(src, this.sess);
		DValue dval = res.getAsDValue();
		assertEquals(null, dval);
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
