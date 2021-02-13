package org.delia.rule;

import static org.junit.Assert.assertEquals;

import org.delia.db.sizeof.DeliaTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

/**
 * It appears that commas are not parsed correctly between rules.
 * 
 * @author Ian Rae
 *
 */
public class RuleSyntaxTests extends DeliaTestBase { 
	
	@Test
	public void test() {
		ruleSrc = "field2.maxlen(20)";
		String src = "let x = Flight[1]";
		execute(src);
		DValue dval = session.getFinalResult().getAsDValue();
		assertEquals(1, dval.asStruct().getField("field1").asInt());
	}	
	
	@Test
	public void test2() {
		ruleSrc = "field2.maxlen(20), uniqueFields(field2)";
		String src = "let x = Flight[1]";
		execute(src);
		DValue dval = session.getFinalResult().getAsDValue();
		assertEquals(1, dval.asStruct().getField("field1").asInt());
	}	
	
	
	//TODO: fix. parser should not accept this.
	@Test
	public void test2MissingComma() {
		ruleSrc = "field2.maxlen(20) uniqueFields(field2)";
		String src = "let x = Flight[1]";
		execute(src);
		DValue dval = session.getFinalResult().getAsDValue();
		assertEquals(1, dval.asStruct().getField("field1").asInt());
	}	
	@Test
	public void test2MissingComma2() {
		ruleSrc = "uniqueFields(field2) uniqueFields(field3)";
		String src = "let x = Flight[1]";
		execute(src);
		DValue dval = session.getFinalResult().getAsDValue();
		assertEquals(1, dval.asStruct().getField("field1").asInt());
		
	}	

	//-------------------------
	private String ruleSrc = "";
	
	@Before
	public void init() {
	}

	@Override
	protected String buildSrc() {
		String s = ruleSrc;
		String src = String.format("type Flight struct {field1 int primaryKey, field2 string, field3 string optional } %s end", s);

		s =  "";
		src += String.format("\n insert Flight {field1: 1, field2: 'a'}");
		src += String.format("\n insert Flight {field1: 2, field2: 'b'}");
		return src;
	}

}
