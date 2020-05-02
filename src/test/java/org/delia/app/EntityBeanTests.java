package org.delia.app;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.delia.api.Delia;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;


public class EntityBeanTests extends NewBDDBase {
	
	/**
	 * Used internally by delia to extract data from
	 * the entity, to store it in the database.
	 *
	 */
	public interface DValueEntity {
		String zrenderFull();
		String zrenderUpdate();
	}
	
	/**
	 * This would normally be code-gen'd.
	 * The entity is completely disconnected from its orignation DValue.
	 * We use constructor injection (and reflection) to build an
	 * entity object from a DValue.
	 * 
	 * We use dirty flags to track changes to the entity object.
	 * Not only is this faster than doing full object comparison,
	 * but it's more correct.
	 *  -if app calls setter and sets to the existing value
	 *  this may have app semantics, and we should persist
	 *  all values that were set (even if they have not changed value).
	 *
	 */
	public static class FlightEntity implements DValueEntity {
		private int field1; //if optional then we use Integer
		private int field2;
		private boolean[] arFlags = new boolean[2]; //2 fields
		
		public FlightEntity(int field1, int field2) {
			this.field1 = field1;
			this.field2 = field2;
		}
		
		//standard getters/setters
		public int getField1() {
			return field1;
		}
		public void setField1(int field1) {
			this.field1 = field1;
			arFlags[0] = true;
		}
		public int getField2() {
			return field2;
		}
		public void setField2(int field2) {
			this.field2 = field2;
			arFlags[1] = true;
		}
		@Override
		public String zrenderFull() {
			//would not include serial primaryKeys, or parent relation values
			String delia = String.format("{field1:%d, field2:%d}", field1, field2);
			return delia;
		}
		@Override
		public String zrenderUpdate() {
			//would not include serial primaryKeys, or parent relation values
			//generate update dson for all changed fields
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			if (arFlags[0]) {
				sb.append("field1");
				sb.append(':');
				sb.append(field1);
			}
			if (arFlags[1]) {
				sb.append("field2");
				sb.append(':');
				sb.append(field2);
			}
			sb.append('}');
			
			return sb.toString();
		}
	}
	
	
	@Test
	public void testEntity() {
		FlightEntity entity = new FlightEntity(10, 11);
		String s = entity.zrenderFull();
		assertEquals("{field1:10, field2:11}", s);
		
		s = entity.zrenderUpdate();
		assertEquals("{}", s);

		entity.setField1(20);
		s = entity.zrenderUpdate();
		assertEquals("{field1:20}", s);
		
		entity.setField2(21);
		s = entity.zrenderUpdate();
		assertEquals("{field1:20, field2:21}", s);
	}
	
	@Test
	public void test1() {
		String src = buildSrc();
		DeliaDao dao = createDao(); 
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

	
	
	//---

	@Before
	public void init() {
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}

	private String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}

}
