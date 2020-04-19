package org.delia.scopetest;

import static org.junit.Assert.assertEquals;

import org.delia.scope.core.MyRunner;
import org.delia.scope.core.Scope;
import org.delia.scopetest.data.AllTypesEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(MyRunner.class)
@Scope("String")
public class StringTests extends BaseScopeTest {
	
	@Test
	@Scope("values")
	public void test() {
//		doCopy("string1");
//		chkValue("abc");
//		
//		reset();
//		entity.setString1("");
//		doCopy("string1");
//		chkValue("");
//		
//		reset();
//		entity.setString1(" ");
//		doCopy("string1");
//		chkValue(" ");
	}
	
	@Test
	@Scope("null")
	public void testNull() {
		entity.setString1(null);
		doCopy("string1");
		assertEquals(null, dto.getString1());
	}
	
	
//	@Test
//	@Scope("Boolean")
//	public void testToBoolean() {
//		copySrcFieldTo(mainField, "primitiveBool");
//		assertEquals(false, dto.isPrimitiveBool());
//	}
//	@Test
//	@Scope("Integer")
//	public void testToInt() {
//		copySrcFieldTo(mainField, "primitiveInt");
//		//TODO: this should really fail. "abc" to int
//		assertEquals(0, dto.getPrimitiveInt());
//		reset();
//		entity.setString1("45");
//		copySrcFieldTo(mainField, "primitiveInt", false);
//		assertEquals(45, dto.getPrimitiveInt());
//		
//		copySrcFieldTo(mainField, "int1");
//		//TODO: this should really fail. "abc" to int
//		assertEquals(0, dto.getInt1().intValue());
//		reset();
//		entity.setString1("45");
//		copySrcFieldTo(mainField, "int1", false);
//		assertEquals(45, dto.getInt1().intValue());
//	}
//	@Test
//	@Scope("Long")
//	public void testToLong() {
//		copySrcFieldTo(mainField, "primitiveLong");
//		//TODO: this should really fail. "abc" to long
//		assertEquals(0, dto.getPrimitiveLong());
//		reset();
//		entity.setString1("45");
//		copySrcFieldTo(mainField, "primitiveLong", false);
//		assertEquals(45, dto.getPrimitiveLong());
//		
//		copySrcFieldTo(mainField, "long1");
//		//TODO: this should really fail. "abc" to long
//		assertEquals(0, dto.getLong1().longValue());
//		reset();
//		entity.setString1("45");
//		copySrcFieldTo(mainField, "long1", false);
//		assertEquals(45, dto.getLong1().longValue());
//	}
//	@Test
//	@Scope("Double")
//	public void testToDouble() {
//		copySrcFieldTo(mainField, "primitiveDouble");
//		//TODO: this should really fail. "abc" to long
//		assertEquals(0.0, dto.getPrimitiveDouble(), 0.0001);
//		reset();
//		entity.setString1("45.2");
//		copySrcFieldTo(mainField, "primitiveDouble", false);
//		assertEquals(45.2, dto.getPrimitiveDouble(), 0.0001);
//		
//		copySrcFieldTo(mainField, "double1");
//		//TODO: this should really fail. "abc" to long
//		assertEquals(0.0, dto.getDouble1().doubleValue(), 0.0001);
//		reset();
//		entity.setString1("45.2");
//		copySrcFieldTo(mainField, "double1", false);
//		assertEquals(45.2, dto.getDouble1().doubleValue(), 0.0001);
//	}
	@Test
	@Scope("String")
	public void testToString() {
//		copySrcFieldTo(mainField, "string1");
//		//TODO: need a date-to-string string converter
//		assertEquals("abc", dto.getString1());
//		
//		reset();
//		entity.setString1("45.2");
//		copySrcFieldTo(mainField, "double1", false);
//		assertEquals(45.2, dto.getDouble1().doubleValue(), 0.0001);
	}
//	@Test
//	@Scope("Date")
//	public void testToDate() {
//		copySrcFieldToFail(mainField, "date1");
//		Date dt = createDate(2015,12,25);
//		assertEquals(null, dto.getDate1());
//
//		reset();
//		entity.setString1("Fri Dec 25 07:30:41 EST 2015");
//		copySrcFieldToFail(mainField, "date1", false);
////		assertEquals(dt, dto.getDate1());
//	}
//	@Test
//	@Scope("enum")
//	public void testToEnum() {
//		copySrcFieldToFail(mainField, "colour1");
//		assertEquals(null, dto.getColour1());
//	}
	
	//---
	private static final String mainField = "string1";
	
	@Before
	public void init() {
		super.init();
	}
	@Override
	protected AllTypesEntity createEntity() {
		AllTypesEntity entity = new AllTypesEntity();
		entity.setString1("abc");
		
		return entity;
	}
	
	protected void chkValue(String s) {
		assertEquals(s, dto.getString1());
	}
}
