package org.delia.dval.compare;

import java.time.ZonedDateTime;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;

public class DValueCompareService extends ServiceBase {
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
			dvalHandlerArray[0][1] = new ToLongHandler();
			dvalHandlerArray[0][2] = new ToNumberHandler();
			dvalHandlerArray[0][3] = new ToStringHandler();
			dvalHandlerArray[0][4] = null; //not supported
			dvalHandlerArray[0][5] = null; //not supported
			
			//long
			dvalHandlerArray[1][0] = new ToLongHandler();
			dvalHandlerArray[1][1] = new ComparableDValueHandler();
			dvalHandlerArray[1][2] = new ToNumberHandler();
			dvalHandlerArray[1][3] = new ToStringHandler();
			dvalHandlerArray[1][4] = null; //not supported
			dvalHandlerArray[1][5] = new ToLongHandler();
			
			//number
			dvalHandlerArray[2][0] = new ToNumberHandler();
			dvalHandlerArray[2][1] = new ToNumberHandler();
			dvalHandlerArray[2][2] = new ComparableDValueHandler();
			dvalHandlerArray[2][3] = new ToStringHandler();
			dvalHandlerArray[2][4] = null; //not supported
			dvalHandlerArray[2][5] = null; //not supported
			
			//string
			dvalHandlerArray[3][0] = new ToStringHandler();
			dvalHandlerArray[3][1] = new ToStringHandler();
			dvalHandlerArray[3][2] = new ToStringHandler();
			dvalHandlerArray[3][3] = new ToStringHandler();
			dvalHandlerArray[3][4] = new ToStringHandler();
			dvalHandlerArray[3][5] = new ToStringHandler();
			
			//boolean
			dvalHandlerArray[4][0] = null; //not supported
			dvalHandlerArray[4][1] = null; //not supported
			dvalHandlerArray[4][2] = null; //not supported
			dvalHandlerArray[4][3] = new ToStringHandler();
			dvalHandlerArray[4][4] = new ToBooleanHandler();
			dvalHandlerArray[4][5] = null; //not supported;
			
			//date
			dvalHandlerArray[5][0] = null; //not supported
			dvalHandlerArray[5][1] = new ToLongHandler();
			dvalHandlerArray[5][2] = null; //not supported
			dvalHandlerArray[5][3] = new ToStringHandler();
			dvalHandlerArray[5][4] = null; //not supported
			dvalHandlerArray[5][5] = new ToLongHandler();
			
		}
		
		public int compare(DValue dval1, DValue dval2) {
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
		public int compareObjs(Object obj1, Object obj2) {
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
			} else if (obj instanceof ZonedDateTime) {
				return 5;
			} else {
				return -1;
			}
		}
	}