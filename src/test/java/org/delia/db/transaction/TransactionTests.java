package org.delia.db.transaction;

import static org.junit.Assert.assertEquals;

import org.delia.db.sizeof.DeliaTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ian Rae
 *
 */
public class TransactionTests extends DeliaTestBase { 
	
	@Test
	public void test() {
		execute(""); //begin cannot run in transaction
		
		String src = "let x = Flight[1]";
		TransactionProvider trans = session.createTransaction();
		trans.beginTransaction();
		try {
			continueExecution(src);
			trans.commitTransaction();
		} catch (Exception e) {
			trans.rollbackTransaction();
		}
		DValue dval = session.getFinalResult().getAsDValue();
		assertEquals(1, dval.asStruct().getField("field1").asInt());
		assertEquals("sfd", dval.asStruct().getField("field2").asString());
	}	
	

	//-------------------------
	@Before
	public void init() {
	}

	@Override
	protected String buildSrc() {
		String s = "";
		String src = String.format("type Flight struct {field1 int primaryKey, field2 string } %s end", s);

		s =  "";
		src += String.format("\n insert Flight {field1: 1, field2: 'abc'}");
		src += String.format("\n insert Flight {field1: 2, field2: 'def'}");
		return src;
	}
}
