package org.delia.scope.scopetest.letscalar;

import static org.junit.Assert.assertEquals;

import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class ScalarTests extends ScopeTestBase {

	//-- int --
	@Test
	public void testInt() {
		DValue dval = runIt("int", "55");
		assertEquals(55, dval.asInt());
	}
	@Test
	public void testInt2() {
		DValue dval = runIt("int", "-55");
		assertEquals(-55, dval.asInt());
	}
	@Test
	public void testInt3() {
		DValue dval = runIt("int", "0");
		assertEquals(0, dval.asInt());
	}
	@Test
	public void testInt4a() {
		Integer n = Integer.MAX_VALUE;
		DValue dval = runIt("int", n.toString());
		assertEquals(n.intValue(), dval.asInt());
	}
	@Test
	public void testInt4b() {
		Integer n = Integer.MIN_VALUE;
		DValue dval = runIt("int", n.toString());
		assertEquals(n.intValue(), dval.asInt());
	}
	
	// -- string --
	@Test
	public void testString() {
		DValue dval = runIt("string", "'abc'");
		assertEquals("abc", dval.asString());
	}
	@Test
	public void testString1() {
		DValue dval = runIt("string", "\"abc\"");
		assertEquals("abc", dval.asString());
	}
	@Test
	public void testString2() {
		DValue dval = runIt("string", "''");
		assertEquals("", dval.asString());
	}
	@Test
	public void testString3() {
		DValue dval = runIt("string", "'©'");
		assertEquals("©", dval.asString());
	}
	
	//-- boolean --
	@Test
	public void testBoolean() {
		DValue dval = runIt("boolean", "false");
		assertEquals(false, dval.asBoolean());
		
		initRunner();
		dval = runIt("boolean", "true");
		assertEquals(true, dval.asBoolean());
	}
	
	//-- long --
	@Test
	public void testLong() {
		DValue dval = runIt("long", "55");
		assertEquals(55, dval.asLong());
		assertEquals(55, dval.asInt());
	}
	@Test
	public void testLong2() {
		DValue dval = runIt("long", "-55");
		assertEquals(-55, dval.asLong());
	}
	@Test
	public void testLong3() {
		DValue dval = runIt("long", "0");
		assertEquals(0, dval.asLong());
	}
	@Test
	public void testLong4a() {
		Long n = Long.MAX_VALUE;
		DValue dval = runIt("long", n.toString());
		assertEquals(n.longValue(), dval.asLong());
	}
	@Test
	public void testLong4b() {
		Long n = Long.MIN_VALUE;
		DValue dval = runIt("long", n.toString());
		assertEquals(n.longValue(), dval.asLong());
	}
	
	//-- number --
	private static final double DELTA = 0.000001;
	@Test
	public void testNumber() {
		DValue dval = runIt("number", "55");
		assertEquals(55.0, dval.asNumber(), DELTA);
	}
	@Test
	public void testNumber2() {
		DValue dval = runIt("number", "-55");
		assertEquals(-55, dval.asNumber(), DELTA);
	}
	@Test
	public void testNumber3() {
		DValue dval = runIt("number", "0");
		assertEquals(0, dval.asNumber(), DELTA);
	}
	@Test
	public void testNumber3a() {
		DValue dval = runIt("number", "1025.3476");
		assertEquals(1025.3476, dval.asNumber(), DELTA);
	}
	@Test
	public void testNumber4a() {
		Long n = Long.MAX_VALUE;
		DValue dval = runIt("number", n.toString());
		assertEquals(n.doubleValue(), dval.asNumber(), DELTA);
	}
	@Test
	public void testNumber4b() {
		Long n = Long.MIN_VALUE;
		DValue dval = runIt("number", n.toString());
		assertEquals(n.doubleValue(), dval.asNumber(), DELTA);
	}
	
	//-- date --
	@Test
	public void testDate() {
		DValue dval = runIt("date", "'1955'");
		assertEquals("1955-01-01T00:00:00.000+0000", dval.asString());
	}
	@Test
	public void testDate2() {
		DValue dval = runIt("date", "'2007-06-11'");
		assertEquals("2007-06-11T00:00:00.000+0000", dval.asString());
	}
	@Test
	public void testDate3() {
		//yyyy-MM-dd'T'HH:mm:ss
		DValue dval = runIt("date", "'2007-06-11T11:15:51'");
		assertEquals("2007-06-11T11:15:51.000+0000", dval.asString());
	}
	
	//---
	@Before
	public void init() {
		initRunner();
	}
}
