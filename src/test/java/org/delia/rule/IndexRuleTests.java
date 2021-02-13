package org.delia.rule;

import static org.junit.Assert.assertEquals;

import org.delia.db.sizeof.DeliaTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ian Rae
 *
 */
public class IndexRuleTests extends DeliaTestBase { 
	
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
		useSrc2 = true;
		String src = "let x = Flight[1]";
		rule = "uniqueFields(field2,field3)";
		addDup = true;
		executeFail(src, "rule-uniqueFields");
	}	

	//-------------------------
	private boolean useSrc2;
	private String rule = "index(field1)";
	private boolean addDup;
	
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

		src += String.format("\n insert Flight {field1: 1, field2: 10}");
		s =  addDup ? "10" : "20";
		src += String.format("\n insert Flight {field1: 2, field2: %s}", s);
		return src;
	}

	private String buildSrc2() {
		String s = rule;
		String src = String.format("type Flight struct {field1 int primaryKey, field2 int, field3 int } %s end", s);

		src += String.format("\n insert Flight {field1: 1, field2: 10, field3: 10}");
		s =  addDup ? "10" : "20";
		src += String.format("\n insert Flight {field1: 2, field2: %s, field3: %s}", s, s);
		return src;
	}

}
