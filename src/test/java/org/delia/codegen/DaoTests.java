package org.delia.codegen;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.app.DaoTestBase;
import org.delia.codegen.sample.Flight;
import org.delia.codegen.sample.FlightImmut;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaDao;
import org.delia.runner.ResultValue;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;
import org.delia.util.TextFileWriter;
import org.junit.Before;
import org.junit.Test;


public class DaoTests extends DaoTestBase {

	public static class FlightDao extends ServiceBase {

		private DeliaSession session;
		private Delia delia;

		public FlightDao(DeliaSession session) {
			super(session.getDelia().getFactoryService());
			this.session = session;
			this.delia = session.getDelia();
		}
		
		public Flight findById(int id) {
			String src = String.format("Flight[%s]", id);
			ResultValue res = doQuery(src);
			return createImmut(res);
		}
		public List<Flight> findAll() {
			String src = String.format("Flight[true]");
			ResultValue res = doQuery(src);
			return createImmutList(res);
		}
		
		
		
		
		protected List<Flight> createImmutList(ResultValue res) {
			List<Flight> list = new ArrayList<>();
			for(DValue dval: res.getAsDValueList()) {
				list.add(new FlightImmut(dval));
			}
			return list;
		}
		protected Flight createImmut(ResultValue res) {
			DValue dval = res.getAsDValue();
			return new FlightImmut(dval);
		}
		protected ResultValue doQuery(String src) {
			log.log("src: %s", src);
			ResultValue res = delia.continueExecution(src, session);
			if (! res.ok) {
				DeliaExceptionHelper.throwError("dao-error", "Query failed: %s", src);
			}
			return res;
		}
		
	}
	
	
	@Test
	public void test() {

		FlightDao dao = new FlightDao(xdao.getMostRecentSession());
		Flight flight = dao.findById(1);
		assertEquals(1, flight.getField1());
		
		List<Flight> list = dao.findAll();
		assertEquals(22, list.size());
	}

	
	//---
	private DeliaDao xdao;

	@Before
	public void init() {
		String src = buildSrc();
		xdao = createDao(); 
		boolean b = xdao.initialize(src);
		assertEquals(true, b);
	}

	private String buildSrc() {
		String src = "type Wing struct {id int primaryKey, width int } end";
		src += "\n type Flight struct {field1 int primaryKey, field2 int, dd date optional, relation wing Wing one optional } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
}
