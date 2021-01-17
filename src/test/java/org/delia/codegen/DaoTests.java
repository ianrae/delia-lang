package org.delia.codegen;


import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.api.DeliaSession;
import org.delia.app.DaoTestBase;
import org.delia.codegen.sample.Flight;
import org.delia.codegen.sample.FlightEntity;
import org.delia.codegen.sample.FlightImmut;
import org.delia.codegen.sample.Wing;
import org.delia.codegen.sample.WingEntity;
import org.delia.codegen.sample.WingImmut;
import org.delia.dao.DeliaGenericDao;
import org.delia.dao.EntityDaoBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;


public class DaoTests extends DaoTestBase {
	
	public static class FlightDao extends EntityDaoBase<Flight> {
		public FlightDao(DeliaSession session) {
			super(session, "Flight");
		}
		
		public Flight findById(int id) {
			return doFindById(id);
		}
		public List<Flight> findAll() {
			return doFindAll();
		}
		
		public void insert(Flight entity) {
			doInsert(entity);
		}
		
		public int update(Flight entity) {
			return doUpdate(entity);
		}

		@Override
		protected Flight createImmutFromDVal(DValue dval) {
			return new FlightImmut(dval);
		}
	}
	public static class WingDao extends EntityDaoBase<Wing> {
		public WingDao(DeliaSession session) {
			super(session, "Wing");
		}
		
		public Wing findById(int id) {
			return doFindById(id);
		}
		public List<Wing> findAll() {
			return doFindAll();
		}
		
		public DValue insert(Wing entity) {
			DValue serialVal = doInsert(entity);
			if (canSetSerialId(entity, serialVal)) {
				WingEntity we = (WingEntity) entity;
				we.setId(serialVal.asInt());
			}
			return serialVal;
		}
		
		public int update(Wing entity) {
			return doUpdate(entity);
		}

		@Override
		protected Wing createImmutFromDVal(DValue dval) {
			return new WingImmut(dval);
		}
	}
	
	@Test
	public void test() {
		FlightDao dao = new FlightDao(xdao.getMostRecentSession());
		Flight flight = dao.findById(1);
		assertEquals(1, flight.getField1());
		
		List<Flight> list = dao.findAll();
		assertEquals(2, list.size());
	}

	@Test
	public void test2() {
		FlightDao dao = new FlightDao(xdao.getMostRecentSession());
		Flight flight = dao.findById(1);
		assertEquals(10, flight.getField2());
		
		FlightEntity entity = new FlightEntity(flight);
		entity.setField2(23);

		int n = dao.update(entity);
		assertEquals(1, n);
		flight = dao.findById(1);
		assertEquals(23, flight.getField2());
	}
	
	@Test
	public void test3() {
		FlightDao dao = new FlightDao(xdao.getMostRecentSession());
		Flight flight = dao.findById(1);
		assertEquals(10, flight.getField2());
		
		FlightEntity entity = new FlightEntity(flight);
		entity.setField1(3);
		entity.setField2(23);

		dao.insert(entity);
		List<Flight> list = dao.findAll();
		assertEquals(3, list.size());
	}
	
	@Test
	public void test3Disconnected() {
		FlightDao dao = new FlightDao(xdao.getMostRecentSession());
		
		FlightEntity entity = new FlightEntity();
		entity.setField1(3);
		entity.setField2(23);

		dao.insert(entity);
		List<Flight> list = dao.findAll();
		assertEquals(3, list.size());
		Flight flight = dao.findById(3);
		assertEquals(23, flight.getField2());
	}
	
	@Test
	public void test4Serial() {
		WingDao dao = new WingDao(xdao.getMostRecentSession());
		
		WingEntity entity = new WingEntity();
		entity.setWidth(40);

		DValue serialVal = dao.insert(entity);
		assertEquals(1, serialVal.asInt());
		assertEquals(1, entity.getId()); //also gets set
		
		List<Wing> list = dao.findAll();
		assertEquals(1, list.size());
		Wing flight = dao.findById(1);
		assertEquals(40, flight.getWidth());
	}
	
	//---
	protected DeliaGenericDao xdao;

	@Before
	public void init() {
		String src = buildSrc();
		xdao = createDao(); 
		boolean b = xdao.initialize(src);
		assertEquals(true, b);
	}

	protected String buildSrc() {
		//one-sided relation
		String src = "type Wing struct {id int primaryKey serial, width int } end";
		src += "\n type Flight struct {field1 int primaryKey, field2 int, dd date optional, relation wing Wing one optional } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
}
