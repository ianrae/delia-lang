package org.delia.codegen;


import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.app.DaoTestBase;
import org.delia.dao.DeliaGenericDao;
import org.delia.runner.ResultValue;
import org.delia.type.DStructHelper;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;


public class EntityTests extends DaoTestBase {
	public interface Flight extends DeliaImmutable {
		int getField1();
		Integer getField2();
	}
	public interface FlightSetter extends DeliaEntity {
		void setField1(int val);
		void setField2(Integer val);
	}
	public static class FlightImmut implements Flight {
		private DValue dval;
		private DStructHelper helper;

		public FlightImmut(DValue dval) {
			this.dval = dval;
			this.helper = dval.asStruct();
		}
		@Override
		public int getField1() {
			return helper.getField("field1").asInt();
		}

		@Override
		public Integer getField2() {
			return helper.getField("field2").asInt();
		}
		//		@Override
		//		public Address getAddr() {
		//			return new AddressImmut(helper.getField("addr"));
		//		}
		@Override
		public DValue internalDValue() {
			return dval;
		}
	}

	public static class FlightEntity implements Flight,FlightSetter {
		private DValue dval;
		private DStructHelper helper;
		private Map<String,Object> setMap = new HashMap<>();

		public FlightEntity(DValue dval) {
			this.dval = dval;
			this.helper = dval.asStruct();
		}
		public FlightEntity(Flight immut) {
			FlightImmut x = (FlightImmut) immut;
			this.dval = x.dval;
			this.helper = dval.asStruct();
		}

		@Override
		public int getField1() {
			String fieldName = "field1";
			if (setMap.containsKey(fieldName)) {
				return (Integer)setMap.get(fieldName); //can return null
			}
			return helper.getField(fieldName).asInt();
		}

		@Override
		public Integer getField2() {
			String fieldName = "field2";
			if (setMap.containsKey(fieldName)) {
				return (Integer)setMap.get(fieldName); //can return null
			}
			return helper.getField(fieldName).asInt();
		}
		@Override
		public void setField1(int val) {
			setMap.put("field1", val);
		}
		@Override
		public void setField2(Integer val) {
			setMap.put("field2", val);
		}
		@Override
		public DValue internalDValue() {
			return dval; //can be null, if disconnected entity
		}
		@Override
		public Map<String, Object> internalSetValueMap() {
			return setMap;
		}
	}


	public class FlightDao {
		private DeliaGenericDao innerDao;
		protected String typeName;

		public FlightDao(Delia delia, DeliaSession session) {
		}

		public Flight queryByPrimaryKey(int primaryKey) {
			ResultValue res = null; //innerDao.queryByPrimaryKey(typeName, primaryKey);
			return new FlightImmut(res.getAsDValue());
		}

		public long count() {
			return innerDao.count(typeName);
		}

		public void insert(Flight flight) {
			//used setInsertPrebuiltValueIterator
			//return innerDao.insertOne(typeName, fields);
			//if flight is entity then set serial pk
		}
		public int mostRecentSerialPK() {
			return 0; //from last insert()
		}

		public int update(Flight flight) {
			//			return innerDao.updateOne(typeName, primaryKey, fields);
			return 0; //# updated rows
		}
		public int save(Flight flight) {
			//if flight is entity then set serial pk
			return 0; //insert or update
		}

		public void delete(Flight flight) {
		}
	}

	@Test
	public void test1() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		ResultValue res = dao.queryByPrimaryKey("Flight", "1");
		assertEquals(true, res.ok);

		Flight flight = new FlightImmut(res.getAsDValue());
		assertEquals(1, flight.getField1());
		assertEquals(10, flight.getField2().intValue());

		FlightEntity entity = new FlightEntity(flight);
		entity.setField1(12);
		assertEquals(12, entity.getField1());
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
