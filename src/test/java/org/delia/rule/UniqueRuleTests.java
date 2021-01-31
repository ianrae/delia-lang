package org.delia.rule;

import static org.junit.Assert.assertEquals;

import org.delia.db.sizeof.DeliaTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

/**
 * rules are built in RuleFuncFactory
 * @author Ian Rae
 *
 */
public class UniqueRuleTests extends DeliaTestBase { 
	
	@Test
	public void test() {
		String src = "let x = Flight[1]";
		execute(src);
		DValue dval = session.getFinalResult().getAsDValue();
		assertEquals(1, dval.asStruct().getField("field1").asInt());
		int n = dval.asStruct().getField("field2").asInt();
		assertEquals(10, n);
	}	
	
	@Test
	public void testFail() {
		String src = "let x = Flight[1]";
		rule = "uniqueFields(field1,field2)";
		execute(src);
		DValue dval = session.getFinalResult().getAsDValue();
		assertEquals(1, dval.asStruct().getField("field1").asInt());
		int n = dval.asStruct().getField("field2").asInt();
		assertEquals(10, n);
	}	

	//-------------------------
	private boolean useSrc2;
	private String rule = "uniqueFields(field1)";

	
	@Before
	public void init() {
	}

	@Override
	protected String buildSrc() {
		if (useSrc2) {
			return buildSrc2();
		}
		String s = rule;
		String src = String.format("type Flight struct {field1 int primaryKey, field2 int } %s end", s);

		s =  "";
		src += String.format("\n insert Flight {field1: 1, field2: 10}");
		src += String.format("\n insert Flight {field1: 2, field2: 20}");
		return src;
	}

	private String buildSrc2() {
		String src = "";
//		String src = String.format("type Flight struct {field1 int primaryKey, field2 blob optional } %s end", s);
//
//		s =  "";
//		src += String.format("\n insert Flight {field1: 1, field2: null }");
//		src += String.format("\n insert Flight {field1: 2, field2: '4E/QIA=='}");
		return src;
	}

}
