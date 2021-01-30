package org.delia.db.sizeof;


import org.delia.db.hld.NewHLSTestBase;
import org.delia.hld.HLDQueryStatement;
import org.junit.Before;
import org.junit.Test;

/**
 * rules are built in RuleFuncFactory
 * @author Ian Rae
 *
 */
public class SizeofTests extends NewHLSTestBase {
	
	@Test
	public void test() {
		String src = "let x = Flight[15]";
		HLDQueryStatement hld = buildFromSrc(src, 0); 
		chkRawSql(hld, "SELECT t0.field1,t0.field2 FROM Flight as t0 WHERE t0.field1=15");
		chkFullSql(hld, "SELECT t0.field1,t0.field2 FROM Flight as t0 WHERE t0.field1=?", "15");
	}	

	


	//-------------------------
	private boolean addSizeof = true;
//	private boolean srcSimpleTypes;

	@Before
	public void init() {
		//createDao();
	}

	@Override
	protected String buildSrc() {
		String s = addSizeof ? "wid.sizeof(8)" : "";
		String src = String.format("type Flight struct {field1 int primaryKey, field2 int } %s end", s);

		s =  "";
		src += String.format("\n insert Flight {field1: 1, field2: 10 %s}", s);
		src += String.format("\n insert Flight {field1: 2, field2: 20 %s}", s);
		return src;
	}


}
