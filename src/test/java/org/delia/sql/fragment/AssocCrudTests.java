//package org.delia.sql.fragment;
//
//
//import static org.junit.Assert.assertEquals;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.StringJoiner;
//
//import org.delia.api.Delia;
//import org.delia.api.DeliaSessionImpl;
//import org.delia.api.MigrationAction;
//import org.delia.compiler.ast.Exp;
//import org.delia.compiler.ast.QueryExp;
//import org.delia.compiler.ast.UpdateStatementExp;
//import org.delia.dao.DeliaGenericDao;
//import org.delia.db.QueryDetails;
//import org.delia.db.QuerySpec;
//import org.delia.db.postgres.PostgresAssocTablerReplacer;
//import org.delia.db.sql.fragment.FragmentParserService;
//import org.delia.db.sql.fragment.UpdateFragmentParser;
//import org.delia.db.sql.fragment.UpdateStatementFragment;
//import org.delia.db.sql.fragment.WhereFragmentGenerator;
//import org.delia.db.sql.prepared.SqlStatement;
//import org.delia.db.sql.prepared.SqlStatementGroup;
//import org.delia.db.sql.table.TableInfo;
//import org.delia.runner.ConversionResult;
//import org.delia.runner.DeliaException;
//import org.delia.runner.RunnerImpl;
//import org.delia.type.DStructType;
//import org.delia.type.DValue;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//
//public class AssocCrudTests extends FragmentParserTestBase {
//	
////	//=============== one-to-one ======================
//	@Test(expected=DeliaException.class)
//	public void testOneToOne() {
//		String src = buildSrcOneToOne();
//		src += "\n  update Customer[true] {wid: 333, insert addr:100}";
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//		
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ?;");
//		chkLine(2, selectFrag, " DELETE FROM AddressCustomerDat1;");
//		chkLine(3, selectFrag, " WITH cte1 AS (SELECT ? as leftv, id as rightv FROM Customer) INSERT INTO AddressCustomerDat1 as t SELECT * from cte1");
//		chkNoLine(4);
//		chkParams(selectFrag, 333,  100);
//		chkNumParams(1, 0, 1);
//	}
//	
////	//=============== one-to-many ======================
//	@Test(expected=DeliaException.class)
//	public void testOneToMany() {
//		String src = buildSrcManyToOne();
//		src += "\n  update Customer[true] {wid: 333, insert addr:100}";
//		src += "\n  update Address[true] {z:5, cust:100}";
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Address");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//		
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ?;");
//		chkLine(2, selectFrag, " DELETE FROM AddressCustomerDat1;");
//		chkLine(3, selectFrag, " WITH cte1 AS (SELECT ? as leftv, id as rightv FROM Customer) INSERT INTO AddressCustomerDat1 as t SELECT * from cte1");
//		chkNoLine(4);
//		chkParams(selectFrag, 333,  100);
//		chkNumParams(1, 0, 1);
//	}
//
////	//=============== many to many ======================
//	@Test(expected=DeliaException.class)
//	public void testAll() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Customer[true] {wid: 333, insert addr:100}";
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//		
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ?;");
//		chkLine(2, selectFrag, " DELETE FROM AddressCustomerDat1;");
//		chkLine(3, selectFrag, " WITH cte1 AS (SELECT ? as leftv, id as rightv FROM Customer) INSERT INTO AddressCustomerDat1 as t SELECT * from cte1");
//		chkNoLine(4);
//		chkParams(selectFrag, 333,  100);
//		chkNumParams(1, 0, 1);
//	}
//	
//	//scenario 2: ID-----------------------------
//	@Test
//	public void testId() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Customer[55] {wid: 333, insert addr:100}";
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " INSERT INTO AddressCustomerDat1 as b (b.leftv, b.rightv) VALUES(?, ?)");
//		chkNoLine(3);
//		chkParams(selectFrag, 333,55, 100,55);
//		chkNumParams(2, 2);
//	}
//	@Test
//	public void testId2() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Customer[55] {wid: 333, insert addr:[100,101]}";
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " INSERT INTO AddressCustomerDat1 as b (b.leftv, b.rightv) VALUES(?, ?);");
//		chkLine(3, selectFrag, " INSERT INTO AddressCustomerDat1 as b (b.leftv, b.rightv) VALUES(?, ?)");
//		chkNoLine(4);
//		chkParams(selectFrag, 333,55, 100,55, 101,55);
//		chkNumParams(2, 2, 2);
//	}
//	@Test
//	public void testId2OtherWash() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Customer[55] {wid: 333, insert addr:[100,101]}";
//
//		List<TableInfo> tblinfoL = createTblInfoL();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " INSERT INTO CustomerAddressDat1 as b (b.leftv, b.rightv) VALUES(?, ?);");
//		chkLine(3, selectFrag, " INSERT INTO CustomerAddressDat1 as b (b.leftv, b.rightv) VALUES(?, ?)");
//		chkNoLine(4);
//		chkParams(selectFrag, 333,55, 55,100, 55,101);
//		chkNumParams(2, 2, 2);
//	}
//	
//	@Test
//	public void testId3() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Address[100] {z: 7, insert cust:55}";
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Address");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " INSERT INTO AddressCustomerDat1 as b (b.leftv, b.rightv) VALUES(?, ?)");
//		chkNoLine(3);
//		chkParams(selectFrag, 7, 100, 100,55);
//		chkNumParams(2, 2);
//	}
//	@Test
//	public void testId3OtherWay() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Address[100] {z: 7, insert cust:55}";
//
//		List<TableInfo> tblinfoL = createTblInfoL();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Address");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " INSERT INTO CustomerAddressDat1 as b (b.leftv, b.rightv) VALUES(?, ?)");
//		chkNoLine(3);
//		chkParams(selectFrag, 7, 100, 55,100);
//		chkNumParams(2, 2);
//	}
//	@Test(expected=DeliaException.class)
//	public void testId5() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Customer[55] {wid: 333, insert addr:null}";
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " INSERT INTO AddressCustomerDat1 as b (b.leftv, b.rightv) VALUES(?, ?)");
//		chkNoLine(3);
//		chkParams(selectFrag, 333,55, 100,55);
//		chkNumParams(2, 2);
//	}
//	
//
//	//scenario 3: OTHER -----------------------------
//	@Test(expected=DeliaException.class)
//	public void testOther() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Customer[wid > 10 and id < 500] {wid: 333, insert addr:100}";
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//		
//		runAndChkLine(1, selectFrag, "DELETE FROM AddressCustomerDat1 WHERE  rightv IN (SELECT id FROM Customer as a WHERE  a.wid > ? and  a.id < ?);");
//		chkLine(2, selectFrag, " WITH cte1 AS (SELECT ? as leftv, id as rightv FROM Customer as a WHERE  a.wid > ? and  a.id < ?) INSERT INTO AddressCustomerDat1 as t SELECT * from cte1;");
//		chkLine(3, selectFrag, " UPDATE Customer as a SET a.wid = ? WHERE  a.wid > ? and  a.id < ?");
//		chkNoLine(4);
//		chkParams(selectFrag, 10,500, 100,10,500, 333,10,500);
//		chkNumParams(2, 3, 3);
//	}
//	
//	
//	//==================== assocCrud delete -----------------------------
//	@Test
//	public void testIdDelete() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Customer[55] {wid: 333, delete addr:100}";
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " DELETE FROM AddressCustomerDat1 as b WHERE leftv = ? and rightv = ?");
//		chkNoLine(3);
//		chkParams(selectFrag, 333,55, 100,55);
//		chkNumParams(2, 2);
//	}
//	@Test
//	public void testIdDelete2() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Customer[55] {wid: 333, delete addr:[100,101]}";
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " DELETE FROM AddressCustomerDat1 as b WHERE leftv = ? and rightv = ?;");
//		chkLine(3, selectFrag, " DELETE FROM AddressCustomerDat1 as b WHERE leftv = ? and rightv = ?");
//		chkNoLine(4);
//		chkParams(selectFrag, 333,55, 100,55, 101,55);
//		chkNumParams(2, 2, 2);
//	}
//	@Test
//	public void testId2OtherWayDelete() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Customer[55] {wid: 333, delete addr:[100,101]}";
//
//		List<TableInfo> tblinfoL = createTblInfoL();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " DELETE FROM CustomerAddressDat1 as b WHERE leftv = ? and rightv = ?;");
//		chkLine(3, selectFrag, " DELETE FROM CustomerAddressDat1 as b WHERE leftv = ? and rightv = ?");
//		chkNoLine(4);
//		chkParams(selectFrag, 333,55, 55,100, 55,101);
//		chkNumParams(2, 2, 2);
//	}
//	
//	@Test
//	public void testId3Delete() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Address[100] {z: 7, delete cust:55}";
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Address");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " DELETE FROM AddressCustomerDat1 as b WHERE leftv = ? and rightv = ?");
//		chkNoLine(3);
//		chkParams(selectFrag, 7, 100, 100,55);
//		chkNumParams(2, 2);
//	}
//	@Test
//	public void testId3OtherWayDelete() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Address[100] {z: 7, delete cust:55}";
//
//		List<TableInfo> tblinfoL = createTblInfoL();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Address");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " DELETE FROM CustomerAddressDat1 as b WHERE leftv = ? and rightv = ?");
//		chkNoLine(3);
//		chkParams(selectFrag, 7, 100, 55,100);
//		chkNumParams(2, 2);
//	}
//	@Test(expected=DeliaException.class)
//	public void testId5Delete() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Customer[55] {wid: 333, delete addr:null}";
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " INSERT INTO AddressCustomerDat1 as b (b.leftv, b.rightv) VALUES(?, ?)");
//		chkNoLine(3);
//		chkParams(selectFrag, 333,55, 100,55);
//		chkNumParams(2, 2);
//	}
//	
//	
//	//==================== assocCrud delete -----------------------------
//	@Test(expected=DeliaException.class)
//	public void testIdUpdateNotPairs() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Customer[55] {wid: 333, update addr:[100]}"; 
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//	}
//	@Test
//	public void testIdUpdate() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Customer[55] {wid: 333, update addr:[100,222]}"; 
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " UPDATE AddressCustomerDat1 as b SET b.leftv = ?, b.rightv = ? WHERE leftv = ? and rightv = ?");
//		chkNoLine(3);
//		chkParams(selectFrag, 333,55, 222,55, 100,55);
//		chkNumParams(2, 4);
//	}
//	@Test
//	public void testIdUpdate2() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Customer[55] {wid: 333, update addr:[100,222,101,223]}";
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " UPDATE AddressCustomerDat1 as b SET b.leftv = ?, b.rightv = ? WHERE leftv = ? and rightv = ?;");
//		chkLine(3, selectFrag, " UPDATE AddressCustomerDat1 as b SET b.leftv = ?, b.rightv = ? WHERE leftv = ? and rightv = ?");
//		chkNoLine(4);
//		chkParams(selectFrag, 333,55, 222,55,100,55, 223,55,101,55);
//		chkNumParams(2, 4, 4);
//	}
//	@Test
//	public void testId2OtherWayUpdate() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Customer[55] {wid: 333, update addr:[100,222,101,223]}";
//
//		List<TableInfo> tblinfoL = createTblInfoL();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " UPDATE CustomerAddressDat1 as b SET b.leftv = ?, b.rightv = ? WHERE leftv = ? and rightv = ?;");
//		chkLine(3, selectFrag, " UPDATE CustomerAddressDat1 as b SET b.leftv = ?, b.rightv = ? WHERE leftv = ? and rightv = ?");
//		chkNoLine(4);
//		chkParams(selectFrag, 333,55, 55,222,55,100,  55,223,55,101);
//		chkNumParams(2, 4, 4);
//	}
//	
//	@Test
//	public void testId3Update() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Address[100] {z: 7, update cust:[55,56]}";
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Address");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " UPDATE AddressCustomerDat1 as b SET b.leftv = ?, b.rightv = ? WHERE leftv = ? and rightv = ?");
//		chkNoLine(3);
//		chkParams(selectFrag, 7,100, 100,56,100,55);
//		chkNumParams(2, 4);
//	}
//	@Test
//	public void testId3OtherWayUpdate() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Address[100] {z: 7, update cust:[55,56]}";
//
//		List<TableInfo> tblinfoL = createTblInfoL();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Address");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, " UPDATE CustomerAddressDat1 as b SET b.leftv = ?, b.rightv = ? WHERE leftv = ? and rightv = ?");
//		chkNoLine(3);
//		chkParams(selectFrag, 7,100, 56,100,55,100);
//		chkNumParams(2, 4);
//	}
//	@Test(expected=DeliaException.class)
//	public void testId5Update() {
//		String src = buildSrcManyToMany();
//		src += "\n  update Customer[55] {wid: 333, update addr:null}";
//
//		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
//		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
//
//	}
//
//	//---
//	private QueryDetails details = new QueryDetails();
//	private boolean useAliasesFlag = true;
//	private UpdateFragmentParser fragmentParser;
//	private ConversionResult recentCres;
//
//
//	@Before
//	public void init() {
//	}
//	@After
//	public void shutdown() {
//	}
//	
//
//	private String buildSrcOneToOne() {
//		String src = " type Customer struct {id int unique, wid int, relation addr Address optional one } end";
//		src += "\n type Address struct {id int unique, z int, relation cust Customer optional one } end";
//		return src;
//	}
//	private String buildSrcManyToOne() {
//		String src = " type Customer struct {id int unique, wid int, relation addr Address optional many } end";
//		src += "\n type Address struct {id int unique, z int, relation cust Customer optional one } end";
//		return src;
//	}
//	private String buildSrcManyToMany() {
//		String src = " type Customer struct {id int unique, wid int, relation addr Address optional many } end";
//		src += "\n type Address struct {id int unique, z int, relation cust Customer optional many } end";
//		return src;
//	}
//
//	private UpdateFragmentParser createFragmentParser(DeliaGenericDao dao, String src, List<TableInfo> tblInfoL) {
//		boolean b = dao.initialize(src);
//		assertEquals(true, b);
//
//		this.delia = dao.getDelia();
//		this.factorySvc = delia.getFactoryService();
//		this.registry = dao.getRegistry();
//		this.runner = new RunnerImpl(factorySvc, dao.getDbInterface());
//
//		UpdateFragmentParser parser = createParser(dao, tblInfoL); 
//		this.queryBuilderSvc = factorySvc.getQueryBuilderService();
//
//		return parser;
//	}
//	private UpdateFragmentParser createParser(DeliaGenericDao dao, List<TableInfo> tblinfoL) {
//		
//		WhereFragmentGenerator whereGen = new WhereFragmentGenerator(factorySvc, registry, runner, null);
//		FragmentParserService fpSvc = createFragmentParserService(whereGen, dao, tblinfoL);
//		PostgresAssocTablerReplacer assocTblReplacer = new PostgresAssocTablerReplacer(factorySvc, fpSvc);
//		UpdateFragmentParser parser = new UpdateFragmentParser(factorySvc, fpSvc, assocTblReplacer);
//		whereGen.tableFragmentMaker = parser;
//		return parser;
//	}
//
//	private void runAndChkLine(int lineNum, UpdateStatementFragment selectFrag, String expected) {
//		SqlStatementGroup stgroup = fragmentParser.renderUpdateGroup(selectFrag);
//		currentGroup = stgroup;
//		for(SqlStatement stat: stgroup.statementL ) {
//			numParamL.add(stat.paramL.size());
//		}
//		
//		//recombine params
//		List<DValue> newL = new ArrayList<>();
//		for(SqlStatement statement: stgroup.statementL) {
//			newL.addAll(statement.paramL);
//		}
//		stgroup.statementL.get(0).paramL = newL;
//		
//		String sql = stgroup.flatten(); //fragmentParser.renderSelect(selectFrag);
//		log.log(sql);
//		if (lineNum == 1) {
//			String[] ar = sql.split("\n");
//			this.sqlLine1 = ar[0];
//			this.sqlLine2 = ar[1];
//			if (ar.length > 2) {
//				this.sqlLine3 = ar[2];
//			}
//			if (ar.length > 3) {
//				this.sqlLine4 = ar[3];
//			}
//			if (ar.length > 4) {
//				this.sqlLine5 = ar[4];
//			}
//			assertEquals(expected, sqlLine1);
//		}
//	}
//	private void chkLine(int lineNum, UpdateStatementFragment selectFrag, String expected) {
//		if (lineNum == 2) {
//			assertEquals(expected, sqlLine2);
//		} else if (lineNum == 3) {
//			assertEquals(expected, sqlLine3);
//		} else if (lineNum == 4) {
//			assertEquals(expected, sqlLine4);
//		} else if (lineNum == 5) {
//			assertEquals(expected, sqlLine5);
//		}
//	}
//
//	private UpdateStatementFragment buildUpdateFragment(UpdateStatementExp exp, DValue dval, Map<String, String> assocCrudMap) {
//		QuerySpec spec= buildQuery((QueryExp) exp.queryExp);
//		fragmentParser.useAliases(useAliasesFlag);
//		UpdateStatementFragment selectFrag = fragmentParser.parseUpdate(spec, details, dval, assocCrudMap);
//		return selectFrag;
//	}
//
//	private UpdateStatementExp buildFromSrc(String src, List<TableInfo> tblinfoL) {
//		DeliaGenericDao dao = createDao(); 
//		Delia xdelia = dao.getDelia();
//		xdelia.getOptions().migrationAction = MigrationAction.GENERATE_MIGRATION_PLAN;
//		dao.getDbInterface().getCapabilities().setRequiresSchemaMigration(true);
//		log.log(src);
//		this.fragmentParser = createFragmentParser(dao, src, tblinfoL); 
//		
//		//		List<Exp> expL = dao.getMostRecentSess().
//		DeliaSessionImpl sessImpl = (DeliaSessionImpl) dao.getMostRecentSession();
//		UpdateStatementExp updateStatementExp = null;
//		for(Exp exp: sessImpl.expL) {
//			if (exp instanceof UpdateStatementExp) {
//				updateStatementExp = (UpdateStatementExp) exp;
//			}
//		}
//		return updateStatementExp;
//	}
//
//	private DValue convertToDVal(UpdateStatementExp updateStatementExp, String typeName) {
//		DStructType structType = (DStructType) registry.getType(typeName);
//		ConversionResult cres = buildPartialValue(structType, updateStatementExp.dsonExp);
//		this.recentCres = cres;
//		assertEquals(0, cres.localET.errorCount());
//		return cres.dval;
//	}
//	//these tests use int params (but they could be long,date,...)
//	private void chkParams(UpdateStatementFragment selectFrag, Integer...args) {
//		StringJoiner joiner = new StringJoiner(",");
//		
//		SqlStatement stat;
//		if (selectFrag.doUpdateLast) {
////			int n = currentGroup.statementL.size();
//			stat = currentGroup.statementL.get(0);
//		} else {
//			stat = selectFrag.statement;
//		}
//		
//		for(DValue dval: stat.paramL) {
//			joiner.add(dval.asString());
//		}
//		log.log("params: " + joiner.toString());
//		
//		assertEquals(args.length, stat.paramL.size());
//		int i = 0;
//		for(Integer arg: args) {
//			DValue dval = stat.paramL.get(i++);
//			assertEquals(arg.intValue(), dval.asInt());
//		}
//	}
//
//}
