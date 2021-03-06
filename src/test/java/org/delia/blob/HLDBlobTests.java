package org.delia.blob;


import static org.junit.Assert.assertEquals;

import org.delia.api.DeliaSessionImpl;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.db.SqlStatement;
import org.delia.db.SqlStatementGroup;
import org.delia.db.hld.NewHLSTestBase;
import org.delia.hld.HLDQueryStatement;
import org.delia.hld.ValType;
import org.delia.hld.cond.FilterCond;
import org.delia.hld.cond.FilterCondBuilder;
import org.delia.hld.cond.FilterVal;
import org.delia.hld.cond.OpFilterCond;
import org.delia.hld.cud.HLDInsertStatement;
import org.delia.runner.DeliaException;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.BlobUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Ian Rae
 *
 */
public class HLDBlobTests extends NewHLSTestBase {
	
	@Test
	public void test() {
		chkbuilderOpBlob("let x = Flight[field3 == '4E/QIA==']", "field3", "==", "4E/QIA==");
	}	
	@Test
	public void testBlobSelect() {
		String src = String.format("let x = Flight[field3 == '4E/QIA==']");
		HLDQueryStatement hld = buildFromSrc(src, 0); 

		SqlStatementGroup stgroup = mgr.generateSql(hld);
		SqlStatement stm = chkGroupGetFirst(stgroup, 1); 
		assertEquals(1, stm.paramL.size());
		
		String hex = BlobUtils.byteArrayToHexString(BlobTests.SMALL);
		assertEquals(hex, stm.paramL.get(0).asString());
		log.log(stm.sql);
		//not alias would normally be present on orderDate
		String s = String.format("SELECT t0.field1,t0.field2,t0.field3 FROM Flight as t0 WHERE t0.field3 = ?");
		assertEquals(s, stm.sql);
	}

	@Test
	public void testBlobInsert() {
		String src = String.format("insert Flight {field1: 3, field2: 30, field3:'4E/QIA=='}");
		
		HLDInsertStatement hld = buildInsertFromSrc(src, 0);

		SqlStatementGroup stgroup = mgr.generateSql(hld);
		SqlStatement stm = chkGroupGetFirst(stgroup, 1); 
		assertEquals(3, stm.paramL.size());
		
		String base64 = BlobUtils.toBase64(BlobTests.SMALL);
		assertEquals(base64, stm.paramL.get(2).asString());
		log.log(stm.sql);
		//not alias would normally be present on orderDate
		String s = String.format("INSERT INTO Flight (field1, field2, field3) VALUES(?, ?, ?)");
		assertEquals(s, stm.sql);
	}
	
	@Test
	public void testCompareNotAllowed() {
		try {
			chkbuilderOpBlob("let x = Flight[field3 < '4E/QIA==']", "field3", "==", "4E/QIA==");
		} catch (DeliaException e) {
			assertEquals("blob-compare-not-allowed", e.getLastError().getId());
			return;
		}
		assertEquals(1,2); //should never get here
	}	
	@Test
	public void testOrderByNotAllowed() {
		try {
			chkbuilderOpBlob("let x = Flight[1].orderBy('field3')", "field3", "==", "4E/QIA==");
		} catch (DeliaException e) {
			assertEquals("blob-orderBy-not-allowed", e.getLastError().getId());
			return;
		}
		assertEquals(1,2); //should never get here
	}	
	@Test
	public void testPKNotAllowed() {
		buildBlobPK = true;
		try {
			chkbuilderOpBlob("let x = Flight[1]", "field3", "==", "4E/QIA==");
		} catch (DeliaException e) {
			assertEquals("primary-key-type-not-allowed", e.getLastError().getId());
			return;
		}
		assertEquals(1,2); //should never get here
	}	
	@Test
	public void testUniqueNotAllowed() {
		buildUnique = true;
		try {
			chkbuilderOpBlob("let x = Flight[1]", "field3", "==", "4E/QIA==");
		} catch (DeliaException e) {
			assertEquals("blob-unique-not-allowed", e.getLastError().getId());
			return;
		}
		assertEquals(1,2); //should never get here
	}	

	@Test
	public void testLet() {
		buildCond("let x = Flight[1].field3");
		DValue dval = session.getFinalResult().getAsDValue();
		assertEquals("4E/QIA==", dval.asString());
		byte[] byteArr = dval.asBlob().getByteArray();
		assertEquals("4E/QIA==", BlobUtils.toBase64(byteArr));
	}	
	
	//-------------------------
	private boolean addBlob = false;
	private boolean buildBlobPK = false;
	private boolean buildUnique = false;

	@Before
	public void init() {
		addBlob = true;
	}
	
	
	private FilterCond buildCond(String src) {
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		DTypeRegistry registry = session.getExecutionContext().registry;
		DStructType dtype = (DStructType) registry.getType("Flight");
		FilterCondBuilder builder = new FilterCondBuilder(registry, dtype, new DoNothingVarEvaluator());
		FilterCond cond = builder.build(queryExp);
		return cond;
	}
	private void chkbuilderOpBlob(String src, String fieldName, String op, String val2) {
		FilterCond cond = buildCond(src);
		OpFilterCond ofc = (OpFilterCond) cond;
		chkSymbol(fieldName, ofc.val1);
		assertEquals(op, ofc.op.toString());
		chkStr(val2, ofc.val2);
	}

	private void chkSymbol(String fieldName, FilterVal fval) {
		assertEquals(ValType.SYMBOL, fval.valType);
		assertEquals(fieldName, fval.asString());
	}
	private void chkStr(String val1, FilterVal fval) {
		assertEquals(ValType.STRING, fval.valType);
		assertEquals(val1, fval.asString());
	}

	@Override
	protected String buildSrc() {
		if (buildBlobPK) {
			return buildBlobPK();
		}
		String uniqueStr = buildUnique ? " unique" : "";
		String s = addBlob ? ", field3 blob" + uniqueStr : "";
		String src = String.format("type Flight struct {field1 int primaryKey, field2 int %s } end", s);

		s = addBlob ? ", field3: '4E/QIA=='" : "";
		src += String.format("\n insert Flight {field1: 1, field2: 10 %s}", s);
		src += String.format("\n insert Flight {field1: 2, field2: 20 %s}", s);
		return src;
	}
	private String buildBlobPK() {
		String src = String.format("type Flight struct {field1 blob primaryKey, field2 int } end");
		return src;
	}
	protected HLDInsertStatement buildInsertFromSrc(String src, int expectedMoreSize) {
		DeliaSessionImpl sessimpl = doCompileStatement(src);
		InsertStatementExp insertExp = findInsert(sessimpl);
		
		log.log(src);
		
		mgr = createManager();
		HLDInsertStatement hld = mgr.fullBuildInsert(insertExp, new DoNothingVarEvaluator(), null, null);
		log.log(hld.toString());
		assertEquals(expectedMoreSize, hld.moreL.size());
		return hld;
	}
	private SqlStatement chkGroupGetFirst(SqlStatementGroup stgroup, int expected) {
		assertEquals(expected, stgroup.size());
		SqlStatement stm = stgroup.getFirst();
		return stm;
	}

}
