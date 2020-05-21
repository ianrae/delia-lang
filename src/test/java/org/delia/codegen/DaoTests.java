package org.delia.codegen;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.api.RunnerInitializer;
import org.delia.app.DaoTestBase;
import org.delia.codegen.sample.Flight;
import org.delia.codegen.sample.FlightEntity;
import org.delia.codegen.sample.FlightImmut;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaDao;
import org.delia.dval.DValueConverterService;
import org.delia.runner.DValueIterator;
import org.delia.runner.ResultValue;
import org.delia.runner.Runner;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.PrimaryKey;
import org.delia.type.TypePair;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;
import org.junit.Before;
import org.junit.Test;


public class DaoTests extends DaoTestBase {
	
	public static class DaoRunnerInitializer implements RunnerInitializer {
		protected DValueIterator iter;
		
		public DaoRunnerInitializer(DValueIterator iter) {
			this.iter = iter;
		}
		
		@Override
		public void initialize(Runner runner) {
			runner.setInsertPrebuiltValueIterator(iter);
		}
	}
	

	public static abstract class EntityDaoBase<T extends DeliaImmutable> extends ServiceBase {
		private DeliaSession mainSession; //not used directly. we create child sessions 
		protected Delia delia;
		protected DValueConverterService dvalConverter;
		protected DTypeRegistry registry;
		protected ScalarValueBuilder scalarBuilder;
		protected String typeName;
		protected DStructType structType;

		public EntityDaoBase(DeliaSession session, String typeName) {
			super(session.getDelia().getFactoryService());
			this.typeName = typeName;
			this.mainSession = session;
			this.delia = session.getDelia();
    		this.registry = session.getExecutionContext().registry;
	    	this.structType = (DStructType) registry.getType(typeName);

			this.dvalConverter = new DValueConverterService(factorySvc);
			this.scalarBuilder = factorySvc.createScalarValueBuilder(registry);
		}
		
		protected ResultValue doInsertOrUpdate(T entity, String src) {
			List<DValue> inputL = new ArrayList<>();
			inputL.add(createDValue(entity));
			DValueIterator iter = new DValueIterator(inputL);	
			DaoRunnerInitializer dri = new DaoRunnerInitializer(iter);
			
			//use child session for thread-safety and isolation
			DeliaSession session = mainSession.createChildSession();
			session.setRunnerIntiliazer(dri);
			ResultValue res = delia.continueExecution(src, session);
			session.setRunnerIntiliazer(null);
			return res;
		}
		
	    protected DValue createDValue(T obj) {
	    	if (obj instanceof DeliaEntity) {
	    		DeliaEntity entity = (DeliaEntity) obj;
	    		return this.buildFromEntity(entity, typeName);
	    	} else {
	    		return obj.internalDValue();
	    	}
	    }

	    protected DValue buildFromEntity(DeliaEntity entity, String typeName) {
	    	if (entity.internalSetValueMap().isEmpty()) {
	    		DeliaImmutable immut = (DeliaImmutable) entity;
	    		return immut.internalDValue();
	    	}
	    		
	    	StructValueBuilder builder = new StructValueBuilder(structType);
	    	for(TypePair pair: structType.getAllFields()) {
	    		String fieldName = pair.name;
	    		if (entity.internalSetValueMap().containsKey(fieldName)) {
	    			Object val = entity.internalSetValueMap().get(fieldName);
	    			if (val instanceof DeliaImmutable) {
	    				DeliaImmutable immut = (DeliaImmutable) val;
	    				DValue dval = immut.internalDValue();
	    				builder.addField(fieldName, dval);
	    			} else {
	    				DValue dval = dvalConverter.buildFromObject(val, pair.type.getShape(), scalarBuilder);
	    				builder.addField(fieldName, dval);
	    			}
	    		} else {
		    		DeliaImmutable immut = (DeliaImmutable) entity;
	    			DValue dval = immut.internalDValue().asMap().get(fieldName); //may get null
	    			builder.addField(fieldName, dval);
	    		}
	    	}

	    	boolean b = builder.finish();
	    	if (!b) {
	    		DeliaExceptionHelper.throwError("badsomething", "ssss");
	    	}
	    	DValue finalVal = builder.getDValue();
	    	return finalVal;
	    }
		
	    protected DValue getPrimaryKeyValue(DeliaImmutable immut) {
			PrimaryKey pk = structType.getPrimaryKey();
			DValue pkval = immut.internalDValue().asStruct().getField(pk.getFieldName());
			return pkval;

	    }
		protected List<T> createImmutList(ResultValue res) {
			List<T> list = new ArrayList<>();
			for(DValue dval: res.getAsDValueList()) {
				list.add(createImmutFromDVal(dval));
			}
			return list;
		}
		protected abstract T createImmutFromDVal(DValue dval);
		
		protected T createImmut(ResultValue res) {
			DValue dval = res.getAsDValue();
			return createImmutFromDVal(dval);
		}
		protected ResultValue doQuery(String src) {
			log.log("src: %s", src);
			//use child session for thread-safety and isolation
			DeliaSession session = mainSession.createChildSession();
			ResultValue res = delia.continueExecution(src, session);
			if (! res.ok) {
				DeliaExceptionHelper.throwError("dao-error", "Query failed: %s", src);
			}
			return res;
		}
		
		
		protected T doFindById(int id) {
			String src = String.format("%s[%s]", typeName, id);
			ResultValue res = doQuery(src);
			return createImmut(res);
		}
		protected List<T> doFindAll() {
			String src = String.format("%s[true]", typeName);
			ResultValue res = doQuery(src);
			return createImmutList(res);
		}
		
		protected void doInsert(T entity) {
			String src = String.format("insert %s {}", typeName);
			doInsertOrUpdate(entity, src);
		}
		
		protected int doUpdate(T entity) {
			DValue pkval = getPrimaryKeyValue(entity);
			String src = String.format("update %s[%s] {}", typeName, pkval.asString());
			ResultValue res = doInsertOrUpdate(entity, src);
			Integer updateCount = (Integer) res.val;
			return updateCount;
		}
		
	}
	
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
	
	//---
	protected DeliaDao xdao;

	@Before
	public void init() {
		String src = buildSrc();
		xdao = createDao(); 
		boolean b = xdao.initialize(src);
		assertEquals(true, b);
	}

	protected String buildSrc() {
		String src = "type Wing struct {id int primaryKey, width int } end";
		src += "\n type Flight struct {field1 int primaryKey, field2 int, dd date optional, relation wing Wing one optional } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
}
