package org.delia.dval;

import static org.junit.Assert.assertEquals;

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
	public static class Handler1 implements Handler {

		@Override
		public int compare(Object obj1, Object obj2) {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}

	public static class DValueCompareService extends ServiceBase {
		private Handler[][] handlerArray = new Handler[6][6];
		
		public DValueCompareService(FactoryService factorySvc) {
			super(factorySvc);
			handlerArray[0][0] = new Handler1();
		}
		
		int compare(DValue dval1, DValue dval2) {
			Shape shape1 = dval1.getType().getShape();
			Shape shape2 = dval2.getType().getShape();
			int i = Shape.getScalarIndex(shape1);
			int j = Shape.getScalarIndex(shape2);
			if (i < 0 || j < 0) {
				DeliaExceptionHelper.throwError("cannot-compare-dval", "Can only compare scalar DValues");
			}
			
			Handler handler = handlerArray[0][0];
			int n = handler.compare(dval1, dval2);
			
			return n;
		}
		
	}

	
	@Test
	public void test() {
		
		DValueCompareService compareSvc = new DValueCompareService(factorySvc);
		ScalarValueBuilder builder = factorySvc.createScalarValueBuilder(registry);
		DValue dval1 = builder.buildInt(4);
		DValue dval2 = builder.buildInt(4);
		int n = compareSvc.compare(dval1, dval2);
		assertEquals(0, n);
		
	}
	
	// --
	private FactoryService factorySvc;
	private Delia delia;
	private DTypeRegistry registry;

	@Before
	public void init() {
		String src = "type Foo struct { } end";
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);
		
		this.factorySvc = dao.getFactorySvc();
		this.delia = dao.getDelia();
		this.registry = dao.getRegistry();
	}
	
}
