package org.delia.db.sizeof;

import org.junit.Before;
import org.junit.Test;

/**
 * rules are built in RuleFuncFactory
 * @author Ian Rae
 *
 */
public class SizeofTests extends DeliaTestBase { 

	@Test
	public void testOK() {
		String src = "let x = Flight[15]";
		execute(src);
	}	
	
	@Test
	public void testOKa() {
//		String src = "let x = Flight[field1 < 15]";
		String src = "let x = Flight[16 > field1]";
		execute(src);
	}	

	@Test
	public void testFailUnknownField() {
		sizeofStr = "wid.sizeof(8)";
		String src = "insert Flight {field1: 3, field2: 256 }";
		executeFail(src, "rule-on-unknown-field");
	}	

	@Test
	public void testSizeof8() {
		chkIt("field2.sizeof(8)", "-128", true);
		chkIt("field2.sizeof(8)", "0", true);
		chkIt("field2.sizeof(8)", "127", true);
	}	
	@Test
	public void testSizeof8Fail() {
		chkIt("field2.sizeof(8)", "-129", false);
		chkIt("field2.sizeof(8)", "128", false);
	}	

	@Test
	public void testSizeof16() {
		chkIt("field2.sizeof(16)", "-32768", true);
		chkIt("field2.sizeof(16)", "0", true);
		chkIt("field2.sizeof(16)", "32767", true);
	}	
	@Test
	public void testSizeof16Fail() {
		chkIt("field2.sizeof(16)", "-32769", false);
		chkIt("field2.sizeof(16)", "32768", false);
	}	
	
	@Test
	public void testSizeof32() {
		chkIt("field2.sizeof(32)", "-2147483648", true);
		chkIt("field2.sizeof(32)", "0", true);
		chkIt("field2.sizeof(32)", "2147483647", true);
	}	
	@Test
	public void testSizeof32Fail() {
		expectedRuleFail = "value-builder-failed";
		chkIt("field2.sizeof(32)", "-2147483649", false);
		chkIt("field2.sizeof(32)", "2147483648", false);
	}	

	@Test
	public void testSizeof64() {
		chkIt("field2.sizeof(64)", "-9223372036854775808", true);
		chkIt("field2.sizeof(64)", "0", true);
		chkIt("field2.sizeof(64)", "9223372036854775807", true);
	}	
	//there are no failing tests for long because that's the biggest physical size delia uses

	//-------------------------
	private boolean addSizeof = true;
	private String sizeofStr = "field2.sizeof(8)";
	private String expectedRuleFail = "rule-sizeof";

	@Before
	public void init() {
	}

	@Override
	protected String buildSrc() {
		String s = addSizeof ? sizeofStr : "";
		String src = String.format("type Flight struct {field1 int primaryKey, field2 int } %s end", s);

		s =  "";
		src += String.format("\n insert Flight {field1: 1, field2: 10 %s}", s);
		src += String.format("\n insert Flight {field1: 2, field2: 20 %s}", s);
		return src;
	}
	private void chkIt(String rule, String s, boolean b) {
		sizeofStr = rule;
		String src = String.format("insert Flight {field1: 3, field2: %s }", s);
		if (b) {
			execute(src);
		} else {
			executeFail(src, expectedRuleFail);
		}
	}


}
