package org.delia.app;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.delia.api.Delia;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.log.Log;
import org.delia.log.StandardLogFactory;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;


public class DeliaDaoTests extends DaoTestBase {
	
	@Test
	public void testRaw() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		//then a controller method
		//insert
		src = String.format("insert Flight {field1: 3, field2: 30}");
		ResultValue res = dao.execute(src);
		assertEquals(true, res.ok);
		
		//query
		String query = String.format("let $$ = Flight[3]");
		res = dao.execute(query);
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(30, dval.asStruct().getField("field2").asInt());
		
		//update
		src = String.format("update Flight[3] {field2: 31}");
		res = dao.execute(src);
		assertEquals(true, res.ok);
		
		//query
		query = String.format("let $$ = Flight[3]");
		res = dao.execute(query);
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertEquals(31, dval.asStruct().getField("field2").asInt());
		
		//delete
		src = String.format("delete Flight[3]");
		res = dao.execute(src);
		assertEquals(true, res.ok);
		
		//query
		query = String.format("let $$ = Flight[3]");
		res = dao.execute(query);
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertNull(dval);
	}

	@Test
	public void test1() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		//then a controller method
		String type = "Flight";
		String id = "3";
		//insert
		ResultValue res = dao.insertOne(type, "field1: 3, field2: 30");
		assertEquals(true, res.ok);
		
		//query
		res = dao.queryByPrimaryKey(type, id);
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(30, dval.asStruct().getField("field2").asInt());
		
		//update
		res = dao.updateOne(type, id, "{field2: 31}");
		assertEquals(true, res.ok);
		
		//query
		res = dao.queryByPrimaryKey(type, id);
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertEquals(31, dval.asStruct().getField("field2").asInt());
		
		//delete
		res = dao.deleteOne(type, id);
		assertEquals(true, res.ok);
		
		//query
		res = dao.queryByPrimaryKey(type, id);
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertNull(dval);
	}

	@Test(expected=DeliaException.class)
	public void testErr() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		//then a controller method
		String type = "Flight";
		//insert
		ResultValue res = dao.insertOne(type, "field1: 3"); //missing field2
		assertEquals(true, res.ok);
	}
	
	@Test
	public void test2() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		//then a controller method
		String type = "Flight";
		
		//query
//		ResultValue res = dao.queryByExpression(type, "field1 > 0");
		ResultValue res = dao.queryByStatement(type, "[field1 > 0].fks()");
		assertEquals(true, res.ok);
		assertEquals(2, res.getAsDValueList().size());
		
		long n = dao.count(type);
		assertEquals(2, n);
	}

	@Test
	public void testErr2() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		//then a controller method
		String type = "Flight";
		
		//query
		String failMsg = null;
		try {
			dao.queryByFilter(type, "zzz > 0");
		} catch (Exception e) {
			log.log(e.getMessage());
			failMsg = e.getMessage();
		}
		assertEquals("memdb-unknown-field: Type 'Flight' doesn't have field 'zzz'", failMsg);
	}
	@Test
	public void testErr3() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		//then a controller method
		String type = "Flight";
		
		//query
		String failMsg = null;
		try {
			dao.queryByFilter("bb", "zzz > 0");
		} catch (Exception e) {
//			e.printStackTrace();
			log.log(e.getMessage());
			failMsg = e.getMessage();
		}
		assertEquals("unknown-type: Type 'bb' doesn't exist", failMsg);
	}
	
	
	@Test
	public void testStandardLog() {
		String src = buildSrc();
		
		StandardLogFactory logFactory = new StandardLogFactory();
		Log slog = logFactory.create(this.getClass());
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).log(slog).build();
		DeliaGenericDao dao = new DeliaGenericDao(delia);
		
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		//then a controller method
		String type = "Flight";
		
		//query
//		ResultValue res = dao.queryByExpression(type, "field1 > 0");
		ResultValue res = dao.queryByStatement(type, "[field1 > 0].fks()");
		assertEquals(true, res.ok);
		assertEquals(2, res.getAsDValueList().size());
	}
	
	@Test
	public void testOp() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		//then a controller method
		String type = "Flight";
		String id = "3";
		//insert
		ResultValue res = dao.insertOne(type, "field1: 3, field2: 30");
		assertEquals(true, res.ok);
		
		//query
		res = dao.queryByFilter(type, "field1 == 3");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(30, dval.asStruct().getField("field2").asInt());
		
	}
	
	
	//---

	@Before
	public void init() {
	}

	private String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
}
