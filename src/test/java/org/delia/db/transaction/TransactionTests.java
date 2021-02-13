package org.delia.db.transaction;

import static org.junit.Assert.assertEquals;

import org.delia.DeliaSession;
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
		DeliaSession tmp = session.runInTransaction(() -> {
			return continueExecution(src);
		});
		DValue dval = session.getFinalResult().getAsDValue();
		assertEquals(1, dval.asStruct().getField("field1").asInt());
		assertEquals("abc", dval.asStruct().getField("field2").asString());
	}	
	
	@Test
	public void testNoReturn() {
		execute(""); //begin cannot run in transaction
		
		String src = "let x = Flight[1]";
		session.runInTransactionVoid(() -> {
			continueExecution(src);
		});
		DValue dval = session.getFinalResult().getAsDValue();
		assertEquals(1, dval.asStruct().getField("field1").asInt());
		assertEquals("abc", dval.asStruct().getField("field2").asString());
	}	
	
	@Test
	public void testExecute() {
		executeInTransaction = true;
		execute(""); //run in transaction
		executeInTransaction = false;

		String src = "let x = Flight[1]";
		continueExecution(src);
		DValue dval = session.getFinalResult().getAsDValue();
		assertEquals(1, dval.asStruct().getField("field1").asInt());
		assertEquals("abc", dval.asStruct().getField("field2").asString());
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
