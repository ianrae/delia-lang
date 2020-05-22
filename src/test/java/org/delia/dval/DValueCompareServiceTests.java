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
		int compareDVal(DValue dval1, DValue dval2);
		int compare(Object obj1, Object obj2);
	}
	public static class ComparableDValueHandler implements Handler {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compareDVal(DValue dval1, DValue dval2) {
			Comparable c1 = (Comparable) dval1.getObject();
			Comparable c2 = (Comparable) dval2.getObject();
			return c1.compareTo(c2);
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Object obj1, Object obj2) {
			Comparable c1 = getComparable(obj1); 
			Comparable c2 = getComparable(obj2); 
			return c1.compareTo(c2);
		}
		private Comparable getComparable(Object obj1) {
			if (obj1 instanceof DValue) {
				DValue dval = (DValue) obj1;
				return (Comparable) dval.getObject();
			}
			return (Comparable) obj1;
		}
	}
	public static class DValueToIntegerHandler implements Handler {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compareDVal(DValue dval1, DValue dval2) {
			Integer n1 = dval1.asInt();
			Integer n2 = dval2.asInt();
			return n1.compareTo(n2);
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Object obj1, Object obj2) {
			Integer n1 = getInteger(obj1);
			Integer n2 = getInteger(obj2);
			return n1.compareTo(n2);
		}
		private Integer getInteger(Object obj1) {
			if (obj1 instanceof DValue) {
				DValue dval = (DValue) obj1;
				return dval.asInt();
			}
			Number n = (Number) obj1;
			return n.intValue();
		}
	}
	public static class DValueToLongHandler implements Handler {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compareDVal(DValue dval1, DValue dval2) {
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
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Object obj1, Object obj2) {
			Long n1 = getLong(obj1);
			Long n2 = getLong(obj2);
			return n1.compareTo(n2);
		}
		private Long getLong(Object obj1) {
			if (obj1 instanceof DValue) {
				DValue dval = (DValue) obj1;
				if (dval.getType().isShape(Shape.DATE)) {
					return dval.asDate().getTime();
				} else {
					return dval.asLong();
				}
			} else if (obj1 instanceof Date) {
				Date dt = (Date) obj1;
				return dt.getTime();
			}
			
			Number n = (Number) obj1;
			return n.longValue();
		}
	}
	public static class DValueToNumberHandler implements Handler {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compareDVal(DValue dval1, DValue dval2) {
			Double n1 = dval1.asNumber();
			Double n2 = dval2.asNumber();
			return n1.compareTo(n2);
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Object obj1, Object obj2) {
			Double n1 = getDouble(obj1);
			Double n2 = getDouble(obj2);
			return n1.compareTo(n2);
		}
		private Double getDouble(Object obj1) {
			if (obj1 instanceof DValue) {
				DValue dval = (DValue) obj1;
				return dval.asNumber();
			}
			Number n = (Number) obj1;
			return n.doubleValue();
		}
	}
	public static class DValueToStringHandler implements Handler {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compareDVal(DValue dval1, DValue dval2) {
			String n1 = dval1.asString();
			String n2 = dval2.asString();
			return n1.compareTo(n2);
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Object obj1, Object obj2) {
			String n1 = obj1.toString();
			String n2 = obj2.toString();
			return n1.compareTo(n2);
		}
	}
	public static class DValueToBooleanHandler implements Handler {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compareDVal(DValue dval1, DValue dval2) {
			Boolean n1 = dval1.asBoolean();
			Boolean n2 = dval2.asBoolean();
			return n1.compareTo(n2);
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Object obj1, Object obj2) {
			Boolean n1 = getBoolean(obj1);
			Boolean n2 = getBoolean(obj2);
			return n1.compareTo(n2);
		}
		private Boolean getBoolean(Object obj1) {
			if (obj1 instanceof DValue) {
				DValue dval = (DValue) obj1;
				return dval.asBoolean();
			}
			
			Boolean b = (Boolean) obj1;
			return b;
		}
	}

	public static class DValueCompareService extends ServiceBase {
		private Handler[][] dvalHandlerArray = new Handler[6][6];
		
		public DValueCompareService(FactoryService factorySvc) {
			super(factorySvc);
			
//		case INTEGER: return 0;
//		case LONG: return 1;
//		case NUMBER: return 2;
//		case STRING: return 3;
//		case BOOLEAN: return 4;
//		case DATE: return 5;
			
			//int
			dvalHandlerArray[0][0] = new ComparableDValueHandler();
			dvalHandlerArray[0][1] = new DValueToLongHandler();
			dvalHandlerArray[0][2] = new DValueToNumberHandler();
			dvalHandlerArray[0][3] = new DValueToStringHandler();
			dvalHandlerArray[0][4] = null; //not supported
			dvalHandlerArray[0][5] = null; //not supported
			
			//long
			dvalHandlerArray[1][0] = new DValueToIntegerHandler();
			dvalHandlerArray[1][1] = new ComparableDValueHandler();
			dvalHandlerArray[1][2] = new DValueToNumberHandler();
			dvalHandlerArray[1][3] = new DValueToStringHandler();
			dvalHandlerArray[1][4] = null; //not supported
			dvalHandlerArray[1][5] = new DValueToLongHandler();
			
			//number
			dvalHandlerArray[2][0] = new DValueToNumberHandler();
			dvalHandlerArray[2][1] = new DValueToNumberHandler();
			dvalHandlerArray[2][2] = new ComparableDValueHandler();
			dvalHandlerArray[2][3] = new DValueToStringHandler();
			dvalHandlerArray[2][4] = null; //not supported
			dvalHandlerArray[2][5] = null; //not supported
			
			//string
			dvalHandlerArray[3][0] = new DValueToStringHandler();
			dvalHandlerArray[3][1] = new DValueToStringHandler();
			dvalHandlerArray[3][2] = new DValueToStringHandler();
			dvalHandlerArray[3][3] = new DValueToStringHandler();
			dvalHandlerArray[3][4] = new DValueToStringHandler();
			dvalHandlerArray[3][5] = new DValueToStringHandler();
			
			//boolean
			dvalHandlerArray[4][0] = null; //not supported
			dvalHandlerArray[4][1] = null; //not supported
			dvalHandlerArray[4][2] = null; //not supported
			dvalHandlerArray[4][3] = new DValueToStringHandler();
			dvalHandlerArray[4][4] = new DValueToBooleanHandler();
			dvalHandlerArray[4][5] = null; //not supported;
			
			//date
			dvalHandlerArray[5][0] = null; //not supported
			dvalHandlerArray[5][1] = new DValueToLongHandler();
			dvalHandlerArray[5][2] = null; //not supported
			dvalHandlerArray[5][3] = new DValueToStringHandler();
			dvalHandlerArray[5][4] = null; //not supported
			dvalHandlerArray[5][5] = new DValueToLongHandler();
			
		}
		
		int compare(DValue dval1, DValue dval2) {
			Shape shape1 = dval1.getType().getShape();
			Shape shape2 = dval2.getType().getShape();
			int i = Shape.getScalarIndex(shape1);
			int j = Shape.getScalarIndex(shape2);
			if (i < 0 || j < 0) {
				DeliaExceptionHelper.throwError("cannot-compare-dval", "Can only compare scalar DValues");
			}
			
			Handler handler = dvalHandlerArray[i][j];
			if (handler == null) {
				DeliaExceptionHelper.throwError("cannot-compare-dval", "Cannot compare scalar shaped %d and %d", i, j);
			}
			int n = handler.compareDVal(dval1, dval2);
			return n;
		}
		int compareObjs(Object obj1, Object obj2) {
			int i = determineShapeIndex(obj1);
			int j = determineShapeIndex(obj2);
			if (i < 0 || j < 0) {
				DeliaExceptionHelper.throwError("cannot-compare-objects", "Can only compare scalar DValues");
			}
			
			Handler handler = dvalHandlerArray[i][j];
			if (handler == null) {
				DeliaExceptionHelper.throwError("cannot-compare-objects", "Cannot compare scalar shaped %d and %d", i, j);
			}
			int n = handler.compare(obj1, obj2);
			return n;
		}

		private int determineShapeIndex(Object obj) {
			if (obj instanceof DValue) {
				DValue dval = (DValue) obj;
				return Shape.getScalarIndex(dval.getType().getShape());
			} else if (obj instanceof Integer) {
				return 0;
			} else if (obj instanceof Long) {
				return 1;
			} else if (obj instanceof Double) {
				return 2;
			} else if (obj instanceof String) {
				return 3;
			} else if (obj instanceof Boolean) {
				return 4;
			} else if (obj instanceof Date) {
				return 5;
			} else {
				return -1;
			}
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
