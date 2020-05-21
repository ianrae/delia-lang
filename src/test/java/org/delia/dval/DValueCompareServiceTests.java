package org.delia.dval;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.delia.api.Delia;
import org.delia.app.DaoTestBase;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaGenericDao;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;

public class DValueCompareServiceTests extends DaoTestBase {
	
	public interface Handler {
		int compare(Object obj1, Object obj2);
	}
	public static class ComparableDValueHandler implements Handler {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Object obj1, Object obj2) {
			DValue dval1 = (DValue) obj1;
			DValue dval2 = (DValue) obj2;
			
			Comparable c1 = (Comparable) dval1.getObject();
			Comparable c2 = (Comparable) dval2.getObject();
			return c1.compareTo(c2);
		}
	}
	public static class ToIntegerHandler implements Handler {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Object obj1, Object obj2) {
			DValue dval1 = (DValue) obj1;
			DValue dval2 = (DValue) obj2;
			
			Integer n1 = dval1.asInt();
			Integer n2 = dval2.asInt();
			return n1.compareTo(n2);
		}
	}
	public static class ToLongHandler implements Handler {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Object obj1, Object obj2) {
			DValue dval1 = (DValue) obj1;
			DValue dval2 = (DValue) obj2;
			
			Long n1;
			if (dval1.getType().isShape(Shape.DATE)) {
				n1 = dval1.asDate().getTime();
			} else {
				n1 = dval1.asLong();
			}
			Long n2;
			if (dval2.getType().isShape(Shape.DATE)) {
				n2 = dval2.asDate().getTime();
			} else {
				n2 = dval2.asLong();
			}
			return n1.compareTo(n2);
		}
	}
	public static class ToNumberHandler implements Handler {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Object obj1, Object obj2) {
			DValue dval1 = (DValue) obj1;
			DValue dval2 = (DValue) obj2;
			
			Double n1 = dval1.asNumber();
			Double n2 = dval2.asNumber();
			return n1.compareTo(n2);
		}
	}
	public static class ToStringHandler implements Handler {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Object obj1, Object obj2) {
			DValue dval1 = (DValue) obj1;
			DValue dval2 = (DValue) obj2;
			
			String n1 = dval1.asString();
			String n2 = dval2.asString();
			return n1.compareTo(n2);
		}
	}

	public static class DValueCompareService extends ServiceBase {
		private Handler[][] handlerArray = new Handler[6][6];
		
		public DValueCompareService(FactoryService factorySvc) {
			super(factorySvc);
			
//		case INTEGER: return 0;
//		case LONG: return 1;
//		case NUMBER: return 2;
//		case STRING: return 3;
//		case BOOLEAN: return 4;
//		case DATE: return 5;
			
			//int
			handlerArray[0][0] = new ComparableDValueHandler();
			handlerArray[0][1] = new ToLongHandler();
			handlerArray[0][2] = new ToNumberHandler();
			handlerArray[0][3] = new ToStringHandler();
			handlerArray[0][4] = null; //not supported
			handlerArray[0][5] = null; //not supported
			
			//long
			handlerArray[1][0] = new ToIntegerHandler();
			handlerArray[1][1] = new ComparableDValueHandler();
			handlerArray[1][2] = new ToNumberHandler();
			handlerArray[1][3] = new ToStringHandler();
			handlerArray[1][4] = null; //not supported
			handlerArray[1][5] = new ToLongHandler();
			
		}
		
		int compare(DValue dval1, DValue dval2) {
			Shape shape1 = dval1.getType().getShape();
			Shape shape2 = dval2.getType().getShape();
			int i = Shape.getScalarIndex(shape1);
			int j = Shape.getScalarIndex(shape2);
			if (i < 0 || j < 0) {
				DeliaExceptionHelper.throwError("cannot-compare-dval", "Can only compare scalar DValues");
			}
			
			Handler handler = handlerArray[i][j];
			int n = handler.compare(dval1, dval2);
			
			return n;
		}
		
	}

	
	@Test
	public void test() {
		DValueCompareService compareSvc = new DValueCompareService(factorySvc);
		DValue dval1 = builder.buildInt(4);
		DValue dval2 = builder.buildInt(4);
		int n = compareSvc.compare(dval1, dval2);
		assertEquals(0, n);
		
		dval1 = builder.buildInt(3);
		n = compareSvc.compare(dval1, dval2);
		assertEquals(-1, n);
	}
	
	@Test
	public void testIntString() {
		DValueCompareService compareSvc = new DValueCompareService(factorySvc);
		DValue dval1 = builder.buildInt(4);
		DValue dval2 = builder.buildString("4");
		int n = compareSvc.compare(dval1, dval2);
		assertEquals(0, n);
		
		dval1 = builder.buildInt(3);
		n = compareSvc.compare(dval1, dval2);
		assertEquals(-1, n);
	}
	@Test
	public void testLong() {
		DValueCompareService compareSvc = new DValueCompareService(factorySvc);
		DValue dval2 = builder.buildDate("2019");
		
		Date dt = dval2.asDate();
		DValue dval1 = builder.buildLong(dt.getTime());
		int n = compareSvc.compare(dval1, dval2);
		assertEquals(0, n);
		
		dval2 = builder.buildInt(3);
		n = compareSvc.compare(dval1, dval2);
		assertEquals(1, n);
	}
	
	
	// --
	private FactoryService factorySvc;
	private Delia delia;
	private DTypeRegistry registry;
	private ScalarValueBuilder builder;

	@Before
	public void init() {
		String src = "type Foo struct { } end";
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);
		
		this.factorySvc = dao.getFactorySvc();
		this.delia = dao.getDelia();
		this.registry = dao.getRegistry();
		this.builder = factorySvc.createScalarValueBuilder(registry);
	}
	
}
