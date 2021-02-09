package org.delia.app;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.delia.ConnectionStringBuilder;
import org.delia.Delia;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.db.DBType;
import org.delia.db.sql.ConnectionString;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;


public class TypeDaoTests extends DaoTestBase {
	
	@Test
	public void test1() {
		String src = buildSrc();
		TypeDao dao = createDao("Flight"); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		//then a controller method
		String id = "3";
		//insert
		ResultValue res = dao.insertOne("field1: 3, field2: 30");
		assertEquals(true, res.ok);
		
		//query
		res = dao.queryByPrimaryKey(id);
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(30, dval.asStruct().getField("field2").asInt());
		
		//update
		res = dao.updateOne(id, "{field2: 31}");
		assertEquals(true, res.ok);
		
		//query
		res = dao.queryByPrimaryKey(id);
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertEquals(31, dval.asStruct().getField("field2").asInt());
		
		//delete
		res = dao.deleteOne(id);
		assertEquals(true, res.ok);
		
		//query
		res = dao.queryByPrimaryKey(id);
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertNull(dval);
	}

	
	//---

	@Before
	public void init() {
	}

	private TypeDao createDao(String typeName) {
		ConnectionString connStr = ConnectionStringBuilder.createMEM();
		Delia delia = DeliaBuilder.withConnection(connStr).build();
		return new TypeDao(typeName, delia);
	}

	private String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
}
