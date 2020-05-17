package org.delia.app;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.delia.dao.DeliaDao;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;


public class EntityBeanTests extends DaoTestBase {
	
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
	 * The entity is completely disconnected from its original DValue.
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
	 * Relations
	 *  -child-side: normal getter/setter for fk
	 *  -parent-side
	 *    getAddr_FK() //returns child FK if .fks() or .fetch() was done, else null
	 *    no setter
	 * -if you do a query using fetch: Customer[55].fetch('addr')
	 * you get back an entity-response object that you can use
	 * to build both object
	 *   Customer cust = entityResponse.getSingleValue(Customer.class);
	 *   Address addr = entityResponse.getSingleValue(Address.class);
	 *  -this will return the address owned by the customer.
	 *  -and if you got back a list of customers
	 *   List<Customer> custL = entityResponse.getList(Customer.class);
	 *   List<Address> addrL = entityResponse.getList(Address.class);
	 *  
	 */
	public static class FlightEntity implements DValueEntity {
		private int field1; //if optional then we use Integer
		private int field2;
		private boolean[] arFlags = new boolean[2]; //2 fields
		//note arFlags is only for declared fields of this class
		//when derive subclass it will have its own arFlags2, arFlags3, etc
		
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
//			String delia = String.format("{field1:%d, field2:%d}", field1, field2);
			
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			zrenderFullFields(sb);
			sb.append('}');
			
			return sb.toString();
		}
		
		//derived class would invoke super.zrenderFullFields(sb) and then add its own
		protected boolean zrenderFullFields(StringBuilder sb) {
			sb.append("field1");
			sb.append(':');
			sb.append(field1);
			sb.append(", field2");
			sb.append(':');
			sb.append(field2);
			return true;
		}

		@Override
		public String zrenderUpdate() {
			//would not include serial primaryKeys, or parent relation values
			//generate update dson for all changed fields
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			zrenderUpdateFields(sb);
			sb.append('}');
			
			return sb.toString();
		}

		protected boolean zrenderUpdateFields(StringBuilder sb) {
			boolean needComma = false;
			if (arFlags[0]) {
				sb.append("field1");
				sb.append(':');
				sb.append(field1);
				needComma = true;
			}
			if (arFlags[1]) {
				sb.append(", field2");
				sb.append(':');
				sb.append(field2);
				needComma = true;
			}
			return needComma;
		}
	}
	
	public static class ChildFlightEntity extends FlightEntity {
		private int field3; //if optional then we use Integer
		private int field4;
		private boolean[] arFlagsChildFlightEntity = new boolean[2]; //2 fields
		//note arFlags is only for delcared fields of this class
		//when derive subclass it will have its own arFlags2, arFlags3, etc
		
		public ChildFlightEntity(int field1, int field2, int field3, int field4) {
			super(field1, field2);
			this.field3 = field3;
			this.field4 = field4;
		}
		
		//standard getters/setters
		//would not include setter for serial primaryKey
		//would not include parent relation values
		public int getField3() {
			return field3;
		}
		public void setField3(int field3) {
			this.field3 = field3;
			arFlagsChildFlightEntity[0] = true;
		}
		public int getField4() {
			return field4;
		}
		public void setField4(int field4) {
			this.field4 = field4;
			arFlagsChildFlightEntity[1] = true;
		}
		@Override
		public String zrenderFull() {
			//would not include serial primaryKeys, or parent relation values
//			String delia = String.format("{field1:%d, field2:%d}", field1, field2);
			
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			zrenderFullFields(sb);
			sb.append('}');
			
			return sb.toString();
		}
		
		//derived class would invoke super.zrenderFullFields(sb) and then add its own
		@Override
		protected boolean zrenderFullFields(StringBuilder sb) {
			boolean needComma = super.zrenderFullFields(sb);
			if (needComma) {
				sb.append(',');
				sb.append(' ');
			}
			
			sb.append("field3");
			sb.append(':');
			sb.append(field3);
			sb.append(", field4");
			sb.append(':');
			sb.append(field4);
			return true;
		}

		@Override
		public String zrenderUpdate() {
			//would not include serial primaryKeys, or parent relation values
			//generate update dson for all changed fields
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			zrenderUpdateFields(sb);
			sb.append('}');
			
			return sb.toString();
		}

		@Override
		protected boolean zrenderUpdateFields(StringBuilder sb) {
			boolean needComma = super.zrenderUpdateFields(sb);
			
			if (arFlagsChildFlightEntity[0]) {
				if (needComma) {
					sb.append(',');
					sb.append(' ');
				}
				sb.append("field3");
				sb.append(':');
				sb.append(field3);
				needComma = true;
			}
			if (arFlagsChildFlightEntity[1]) {
				if (needComma) {
					sb.append(',');
					sb.append(' ');
				}
				sb.append("field4");
				sb.append(':');
				sb.append(field4);
				needComma = true;
			}
			return needComma;
		}
	}
	
	/*
	 * how about the Dao?
	 * -use a TypeDao?
	 * 
	 *  ResultValue res = dao.findByPrimaryKey(44);
	 *  FlightEntity entity = dao.buildEntity(res, FlightEntity.class);
	 * 
	 * -or codegen a FlightDao? 
	 *   FlightDao ...
	 *   Flight entity = dao.queryById(10).orderBy("lastName").findOne(); //id is name of primary key field
	 *     .findOne returns single entity
	 *     .findMany returns a list
	 *     .findAny returns null or one entity
	 *     .find() returns EntityResponse from which you can get one or many
	 *   
	 *   EntityResponse resp = dao.doQuery(...)
	 *   Customer cust = resp...(Customer.class)
	 *   Address addr = resp..., Address.class)
	 *   
	 *   dao.insert(cust) of List<Customer> or stream of customer
	 *   n = dao.update(cust) //cust must have been produced by a query (we need correct dirty flags)
	 *   dao.delete(cust)
	 *   
	 *   dao needs so support callling user fns. that's a big selling point of delia
	 *   -user functions are where we can do lots of sql optimization
	 *   EntityResp resp = dao.invokeFunction("foo", 33, "abc");
	 */
	
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
	public void testChildEntity() {
		ChildFlightEntity entity = new ChildFlightEntity(10, 11, 12, 13);
		String s = entity.zrenderFull();
		assertEquals("{field1:10, field2:11, field3:12, field4:13}", s);
		
		s = entity.zrenderUpdate();
		assertEquals("{}", s);

		entity.setField1(20);
		s = entity.zrenderUpdate();
		assertEquals("{field1:20}", s);
		
		entity.setField4(23);
		s = entity.zrenderUpdate();
		assertEquals("{field1:20, field4:23}", s);
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

	private String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
}
