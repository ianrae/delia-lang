package org.delia.dval;

import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.util.Date;

import org.delia.api.Delia;
import org.delia.app.DaoTestBase;
import org.delia.core.FactoryService;
import org.delia.dao.DeliaGenericDao;
import org.delia.dval.compare.DValueCompareService;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;

public class DValueCompareServiceTests extends DaoTestBase {
	
	@Test
	public void test() {
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
		DValue dval2 = builder.buildDate("2019");
		
		ZonedDateTime dt = dval2.asDate();
		DValue dval1 = builder.buildLong(dt.toEpochSecond());
		int n = compareSvc.compare(dval1, dval2);
		assertEquals(0, n);
		
		dval2 = builder.buildInt(3);
		n = compareSvc.compare(dval1, dval2);
		assertEquals(1, n);
	}
	@Test
	public void testLong2() {
		DValue dval1 = builder.buildLong(getLongNum());
		DValue dval2 = builder.buildInt(22);
		
		chkGT(dval1, dval2);
		chkLT(dval2, dval1);
		chkEQ(dval1, dval1);
	}
	@Test
	public void testNumber() {
		DValue dval1 = builder.buildNumber(123.45);
		DValue dval2 = builder.buildInt(22);
		
		chkGT(dval1, dval2);
		chkLT(dval2, dval1);
		chkEQ(dval1, dval1);
	}
	@Test
	public void testNumber2() {
		DValue dval1 = builder.buildNumber(123.0);
		DValue dval2 = builder.buildInt(123);
		
		chkEQ(dval1, dval2);
		chkEQ(dval2, dval1);
		chkEQ(dval1, dval1);
		
		dval2 = builder.buildString("123.0");
		chkEQ(dval1, dval2);
		chkEQ(dval2, dval1);
		chkEQ(dval1, dval1);
	}
	
	@Test
	public void testBoolean() {
		DValue dval1 = builder.buildBoolean(true);
		DValue dval2 = builder.buildBoolean(false);
		
		chkGT(dval1, dval2);
		chkLT(dval2, dval1);
		chkEQ(dval1, dval1);
		
		dval2 = builder.buildString("false");
		chkGT(dval1, dval2);
		chkLT(dval2, dval1);
		chkEQ(dval1, dval1);
	}

	@Test
	public void testDate() {
		DValue dval1 = builder.buildDate("2019");
		DValue dval2 = builder.buildDate("2018");
		
		chkGT(dval1, dval2);
		chkLT(dval2, dval1);
		chkEQ(dval1, dval1);
		
		dval2 = builder.buildString("2019-01-01T00:00:00.000+0000");
		chkEQ(dval1, dval2);
		chkEQ(dval2, dval1);
		chkEQ(dval1, dval1);
	}

	// --
	private FactoryService factorySvc;
	private Delia delia;
	private DTypeRegistry registry;
	private ScalarValueBuilder builder;
	private DValueCompareService compareSvc;

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
		this.compareSvc = new DValueCompareService(factorySvc);
	}
	private void chkGT(DValue dval1, DValue dval2) {
		int n = compareSvc.compare(dval1, dval2);
		assertEquals(true, n > 0);
	}
	private void chkLT(DValue dval1, DValue dval2) {
		int n = compareSvc.compare(dval1, dval2);
		assertEquals(true, n < 0);
	}
	private void chkEQ(DValue dval1, DValue dval2) {
		int n = compareSvc.compare(dval1, dval2);
		assertEquals(0, n);
	}

	private Long getLongNum() {
		int max = Integer.MAX_VALUE;
		long bigId = Long.valueOf((long)max) + 10; //2147483647
		return bigId;
	}


}
