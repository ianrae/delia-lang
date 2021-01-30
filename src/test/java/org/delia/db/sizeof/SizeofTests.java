package org.delia.db.sizeof;

import static org.junit.Assert.assertEquals;

import org.delia.runner.DeliaException;
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
	public void testFailUnknownField() {
		sizeofStr = "wid.sizeof(8)";
		String src = "insert Flight {field1: 3, field2: 256 }";
		executeFail(src, "rule-on-unknown-field");
	}	

	@Test
	public void testFailSizeof() {
		sizeofStr = "field2.sizeof(8)";
		String src = "insert Flight {field1: 3, field2: 256 }";
		executeFail(src, "rule-sizeof");
	}	


	//-------------------------
	private boolean addSizeof = true;
	private String sizeofStr = "field2.sizeof(8)";

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

}
