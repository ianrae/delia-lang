package org.delia.db.transaction;

import static org.junit.Assert.assertEquals;

import org.delia.DeliaSession;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.db.h2.H2ConnectionHelper;
import org.delia.db.h2.test.H2TestCleaner;
import org.delia.db.sizeof.DeliaTestBase;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ian Rae
 *
 */
public class H2TransactionTests extends DeliaTestBase { 
	
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
	
//	@Test
//	public void testNoReturn() {
//		execute(""); //begin cannot run in transaction
//		
//		String src = "let x = Flight[1]";
//		session.runInTransactionVoid(() -> {
//			continueExecution(src);
//		});
//		DValue dval = session.getFinalResult().getAsDValue();
//		assertEquals(1, dval.asStruct().getField("field1").asInt());
//		assertEquals("abc", dval.asStruct().getField("field2").asString());
//	}	
//	
//	@Test
//	public void testExecute() {
//		executeInTransaction = true;
//		execute(""); //run in transaction
//		executeInTransaction = false;
//
//		String src = "let x = Flight[1]";
//		continueExecution(src);
//		DValue dval = session.getFinalResult().getAsDValue();
//		assertEquals(1, dval.asStruct().getField("field1").asInt());
//		assertEquals("abc", dval.asStruct().getField("field2").asString());
//	}	
//	
//	
	

	//-------------------------
	@Before
	public void init() {
		alreadyCreatedDao = createDao();
		cleanTables();
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
	
	@Override
	protected DeliaGenericDao createDao() {
		ConnectionDefinition connStr = H2ConnectionHelper.getTestDB();
		this.delia = DeliaBuilder.withConnection(connStr).build();
		System.out.println("creating dao..");
		return new DeliaGenericDao(delia);
	}
	
	private void cleanTables() {
		H2TestCleaner cleaner = new H2TestCleaner(DBType.H2);
		cleaner.deleteKnownTables(delia.getFactoryService(), delia.getDBInterface());
	}

}
