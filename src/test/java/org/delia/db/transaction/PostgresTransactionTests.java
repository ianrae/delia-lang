package org.delia.db.transaction;

import static org.junit.Assert.assertEquals;

import org.delia.DeliaSession;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.db.h2.test.H2TestCleaner;
import org.delia.db.postgres.PostgresConnectionHelper;
import org.delia.db.sizeof.DeliaTestBase;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ian Rae
 *
 */
public class PostgresTransactionTests extends DeliaTestBase { 
	
	@Test
	public void testCommit() {
		execute(""); 
		
		final String src = buildMoreSrc(3);
		DeliaSession tmp = session.runInTransaction(() -> {
			return continueExecution(src);
		});
		
		String src2 = "let x = Flight[true].count()";
		tmp = continueExecution(src2);
		ResultValue res = tmp.getFinalResult();
		Long n = (Long) res.getAsDValue().asLong();
		assertEquals(4, n.intValue());
	}	
	
	@Test
	public void testRollback() {
		execute(""); 
		
		final String src = buildMoreSrc(3);
		DeliaSession tmp;
		try {
			tmp = session.runInTransaction(() -> {
				continueExecution(src);
				throw new IllegalArgumentException("bam!");
			});
		} catch (IllegalArgumentException e) {
			log.log("rollback should have occurred!");
		}
		
		String src2 = "let x = Flight[true].count()";
		tmp = continueExecution(src2);
		ResultValue res = tmp.getFinalResult();
		Long n = (Long) res.getAsDValue().asLong();
		assertEquals(2, n.intValue());
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

	@Test
	public void testSchema() {
		defaultSchema = "gg"; //schema must already exist
		execute("");

		final String src = buildMoreSrc(3);
		DeliaSession tmp = session.runInTransaction(() -> {
			return continueExecution(src);
		});

		String src2 = "let x = Flight[true].count()";
		tmp = continueExecution(src2);
		ResultValue res = tmp.getFinalResult();
		Long n = (Long) res.getAsDValue().asLong();
		assertEquals(4, n.intValue());
	}



	//-------------------------
	@Before
	public void init() {
		disableAllSlowTestsIfNeeded();
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
	protected String buildMoreSrc(int startId) {
		String src = "";
		int id = startId;
		src += String.format("\n insert Flight {field1: %d, field2: 'abc'}", id);
		src += String.format("\n insert Flight {field1: %d, field2: 'def'}", id+1);
		return src;
	}
	
	@Override
	protected DeliaGenericDao createDao() {
		ConnectionDefinition connStr = PostgresConnectionHelper.getTestDB();
		this.delia = DeliaBuilder.withConnection(connStr).build();
		System.out.println("creating dao..");
		return new DeliaGenericDao(delia);
	}
	
	private void cleanTables() {
		H2TestCleaner cleaner = new H2TestCleaner(DBType.POSTGRES);
		cleaner.deleteKnownTables(delia.getFactoryService(), delia.getDBInterface());
		cleaner.deleteSchemaGG(delia.getFactoryService(), delia.getDBInterface());
	}

}
