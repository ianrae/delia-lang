package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.delia.api.Delia;
import org.delia.api.DeliaSessionImpl;
import org.delia.api.MigrationAction;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.DsonExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.core.FactoryService;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.SqlHelperFactory;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.sql.fragment.UpdateFragmentParser;
import org.delia.db.sql.fragment.UpdateStatementFragment;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.db.sql.table.TableInfo;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.LogLevel;
import org.delia.runner.ConversionResult;
import org.delia.runner.DsonToDValueConverter;
import org.delia.runner.Runner;
import org.delia.runner.RunnerImpl;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigServiceImpl;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;


public class UpdateFragmentParserManyToManyTests extends NewBDDBase {

	//=============== many to many ======================
	//scenario 1: ALL
	@Test
	public void testNoManyToMany() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[55] {wid: 333}";

		UpdateStatementExp updateStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChk(selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?");
		chkParams(selectFrag, 333, 55);
	}
	@Test
	public void testParentAll() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[true] {wid: 333, addr:100}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ?;");
//		chkLine(2, selectFrag, "UPDATE AddressCustomerAssoc as b SET b.leftv = ?");
//		chkParams(selectFrag, 333, 100);
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ?;");
		chkLine(2, selectFrag, " DELETE FROM AddressCustomerAssoc WHERE leftv <> ?;");
		chkLine(3, selectFrag, " MERGE INTO AddressCustomerAssoc as t USING (SELECT id FROM Customer as a) as s ON t.rightv = s.id");
		chkLine(4, selectFrag, " WHEN MATCHED THEN UPDATE SET t.leftv = ?");
		chkLine(5, selectFrag, " WHEN NOT MATCHED THEN INSERT (leftv,rightv) VALUES (?,s.id)");
		chkParams(selectFrag, 333, 100, 100, 100);
	}
	@Test
	public void testParentAllOtherWay() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[true] {wid: 333, addr:100}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ?;");
//		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.rightv = ?");
//		chkParams(selectFrag, 333, 100);
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ?;");
		chkLine(2, selectFrag, " DELETE FROM CustomerAddressAssoc WHERE rightv <> ?;");
		chkLine(3, selectFrag, " MERGE INTO CustomerAddressAssoc as t USING (SELECT id FROM Customer as a) as s ON t.leftv = s.id");
		chkLine(4, selectFrag, " WHEN MATCHED THEN UPDATE SET t.rightv = ?");
		chkLine(5, selectFrag, " WHEN NOT MATCHED THEN INSERT (leftv,rightv) VALUES (s.id,?)");
		chkParams(selectFrag, 333, 100, 100, 100);
		
	}
	@Test
	public void testParentAll3() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[true] {z: 7, cust:55}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ?;");
//		chkLine(2, selectFrag, "UPDATE AddressCustomerAssoc as b SET b.rightv = ?");
//		chkParams(selectFrag, 7, 55);
		
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ?;");
		chkLine(2, selectFrag, " DELETE FROM AddressCustomerAssoc WHERE rightv <> ?;");
		chkLine(3, selectFrag, " MERGE INTO AddressCustomerAssoc as t USING (SELECT id FROM Address as a) as s ON t.leftv = s.id");
		chkLine(4, selectFrag, " WHEN MATCHED THEN UPDATE SET t.rightv = ?");
		chkLine(5, selectFrag, " WHEN NOT MATCHED THEN INSERT (leftv,rightv) VALUES (s.id,?)");
		chkParams(selectFrag, 7, 55, 55, 55);
	}
	@Test
	public void testParentAll4() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[true] {z: 7, cust:55}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ?;");
//		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.leftv = ?");
//		chkParams(selectFrag, 7, 55);

//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ?;");
//		chkLine(2, selectFrag, "DELETE FROM CustomerAddressAssoc WHERE leftv <> ?");
//		chkNoLine(3); 
//		chkParams(selectFrag, 7, 55);
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ?;");
		chkLine(2, selectFrag, " DELETE FROM CustomerAddressAssoc WHERE leftv <> ?;");
		chkLine(3, selectFrag, " MERGE INTO CustomerAddressAssoc as t USING (SELECT id FROM Address as a) as s ON t.rightv = s.id");
		chkLine(4, selectFrag, " WHEN MATCHED THEN UPDATE SET t.leftv = ?");
		chkLine(5, selectFrag, " WHEN NOT MATCHED THEN INSERT (leftv,rightv) VALUES (?,s.id)");
		chkParams(selectFrag, 7, 55, 55, 55);
	}
	@Test
	public void testParentAll5() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[true] {wid: 333, addr:null}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ?;");
//		chkLine(2, selectFrag, "UPDATE AddressCustomerAssoc as b SET b.leftv = ?");
//		chkParams(selectFrag, 333, 100);
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ?;");
		chkLine(2, selectFrag, " DELETE FROM AddressCustomerAssoc");
		chkParams(selectFrag, 333);
	}
	@Test
	public void testParentAll6() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[true] {wid: 333, addr:null}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ?;");
//		chkLine(2, selectFrag, "UPDATE AddressCustomerAssoc as b SET b.leftv = ?");
//		chkParams(selectFrag, 333, 100);
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ?;");
		chkLine(2, selectFrag, " DELETE FROM CustomerAddressAssoc");
		chkParams(selectFrag, 333);
	}
	
	
	//scenario 2: ID-----------------------------
	@Test
	public void testParentId() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[55] {wid: 333, addr:100}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 

//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, "UPDATE AddressCustomerAssoc as b SET b.leftv = ? WHERE b.rightv = ?");
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " DELETE FROM AddressCustomerAssoc WHERE rightv = ? and leftv <> ?;");
		chkLine(3, selectFrag, " MERGE INTO AddressCustomerAssoc KEY(rightv) VALUES(?,?)");
		chkParams(selectFrag, 333, 55, 55, 100, 100, 55);
	}
	@Test
	public void testParentIdOtherWay() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[55] {wid: 333, addr:100}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.rightv = ? WHERE b.leftv = ?");
//		chkParams(selectFrag, 333, 55, 100, 55);
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " DELETE FROM CustomerAddressAssoc WHERE leftv = ? and rightv <> ?;");
		chkLine(3, selectFrag, " MERGE INTO CustomerAddressAssoc KEY(leftv) VALUES(?,?)");
		chkParams(selectFrag, 333, 55, 55, 100, 55, 100);
	}
	@Test
	public void testParentId3() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[100] {z: 7, cust:55}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, "UPDATE AddressCustomerAssoc as b SET b.rightv = ? WHERE b.leftv = ?");
//		chkParams(selectFrag, 7, 100, 55, 100);
		
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " DELETE FROM AddressCustomerAssoc WHERE leftv = ? and rightv <> ?;");
		chkLine(3, selectFrag, " MERGE INTO AddressCustomerAssoc KEY(leftv) VALUES(?,?)");
		chkParams(selectFrag, 7, 100, 100, 55, 100, 55);
	}
	@Test
	public void testParentId4() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[100] {z: 7, cust:55}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.leftv = ? WHERE b.rightv = ?");
//		chkParams(selectFrag, 7, 100, 55, 100);
		
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " DELETE FROM CustomerAddressAssoc WHERE rightv = ? and leftv <> ?;");
		chkLine(3, selectFrag, " MERGE INTO CustomerAddressAssoc KEY(rightv) VALUES(?,?)");
		chkParams(selectFrag, 7, 100, 100, 55, 55, 100);
	}
	@Test
	public void testParentId5() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[100] {z: 7, cust:null}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.leftv = ? WHERE b.rightv = ?");
//		chkParams(selectFrag, 7, 100, 55, 100);
		
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " DELETE FROM AddressCustomerAssoc WHERE leftv = ?");
		chkParams(selectFrag, 7, 100, 100);
	}
	@Test
	public void testParentId6() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[100] {z: 7, cust:null}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
//		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.leftv = ? WHERE b.rightv = ?");
//		chkParams(selectFrag, 7, 100, 55, 100);
		
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " DELETE FROM CustomerAddressAssoc WHERE rightv = ?");
		chkParams(selectFrag, 7, 100, 100);
	}
	

	//scenario 3: OTHER -----------------------------
	@Test
	public void testParentOther() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[wid > 10 and id < 500] {wid: 333, addr:100}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE  a.wid > ? and  a.id < ?;");
//		chkLine(2, selectFrag, "UPDATE AddressCustomerAssoc as b SET b.leftv = ? WHERE b.rightv IN (SELECT id FROM Customer as a WHERE  a.wid > ? and  a.id < ?)");
//		chkParams(selectFrag, 333, 10, 500, 100, 10, 500);
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE  a.wid > ? and  a.id < ?;");
		chkLine(2, selectFrag, " DELETE FROM AddressCustomerAssoc WHERE  b.rightv IN (SELECT id FROM Customer as a WHERE  a.wid > ? and  a.id < ?);");
		chkLine(3, selectFrag, " MERGE INTO AddressCustomerAssoc as t USING (SELECT id FROM Customer as a WHERE  a.wid > ? and  a.id < ?) as s ON t.rightv = s.id");
		chkLine(4, selectFrag, " WHEN MATCHED THEN UPDATE SET t.leftv = ?");
		chkLine(5, selectFrag, " WHEN NOT MATCHED THEN INSERT (leftv,rightv) VALUES (?,s.id)");
		chkParams(selectFrag, 333, 10, 500, 10, 500, 10, 500, 100, 100);
	}
	@Test
	public void testParentOtherOtherWay() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[wid > 10 and id < 500] {wid: 333, addr:100}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE  a.wid > ? and  a.id < ?;");
//		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.rightv = ? WHERE b.leftv IN (SELECT id FROM Customer as a WHERE  a.wid > ? and  a.id < ?)");
//		chkParams(selectFrag, 333, 10, 500, 100, 10, 500);
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE  a.wid > ? and  a.id < ?;");
		chkLine(2, selectFrag, " DELETE FROM CustomerAddressAssoc WHERE  b.leftv IN (SELECT id FROM Customer as a WHERE  a.wid > ? and  a.id < ?);");
		chkLine(3, selectFrag, " MERGE INTO CustomerAddressAssoc as t USING (SELECT id FROM Customer as a WHERE  a.wid > ? and  a.id < ?) as s ON t.leftv = s.id");
		chkLine(4, selectFrag, " WHEN MATCHED THEN UPDATE SET t.rightv = ?");
		chkLine(5, selectFrag, " WHEN NOT MATCHED THEN INSERT (leftv,rightv) VALUES (s.id,?)");
		chkParams(selectFrag, 333, 10, 500, 10, 500, 10, 500, 100, 100);
	}
	@Test
	public void testParentOther3() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[z > 10] {z: 7, cust:55}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.z > ?;");
//		chkLine(2, selectFrag, "UPDATE AddressCustomerAssoc as b SET b.rightv = ? WHERE b.leftv IN (SELECT id FROM Address as a WHERE a.z > ?)");
//		chkParams(selectFrag, 7,10,55, 10);
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.z > ?;");
		chkLine(2, selectFrag, " DELETE FROM AddressCustomerAssoc WHERE  b.leftv IN (SELECT id FROM Address as a WHERE a.z > ?);");
		chkLine(3, selectFrag, " MERGE INTO AddressCustomerAssoc as t USING (SELECT id FROM Address as a WHERE a.z > ?) as s ON t.leftv = s.id");
		chkLine(4, selectFrag, " WHEN MATCHED THEN UPDATE SET t.rightv = ?");
		chkLine(5, selectFrag, " WHEN NOT MATCHED THEN INSERT (leftv,rightv) VALUES (s.id,?)");
		chkParams(selectFrag, 7, 10, 10, 10, 55, 55);
	}
	@Test
	public void testParentOther4() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[z > 10] {z: 7, cust:55}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.z > ?;");
//		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.leftv = ? WHERE b.rightv IN (SELECT id FROM Address as a WHERE a.z > ?)");
//		chkParams(selectFrag, 7,10,55, 10);
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.z > ?;");
		chkLine(2, selectFrag, " DELETE FROM CustomerAddressAssoc WHERE  b.rightv IN (SELECT id FROM Address as a WHERE a.z > ?);");
		chkLine(3, selectFrag, " MERGE INTO CustomerAddressAssoc as t USING (SELECT id FROM Address as a WHERE a.z > ?) as s ON t.rightv = s.id");
		chkLine(4, selectFrag, " WHEN MATCHED THEN UPDATE SET t.leftv = ?");
		chkLine(5, selectFrag, " WHEN NOT MATCHED THEN INSERT (leftv,rightv) VALUES (?,s.id)");
		chkParams(selectFrag, 7, 10, 10, 10, 55, 55);
	}
	@Test
	public void testParentOther5() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[z > 10] {z: 7, cust:null}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.z > ?;");
//		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.leftv = ? WHERE b.rightv IN (SELECT id FROM Address as a WHERE a.z > ?)");
//		chkParams(selectFrag, 7,10,55, 10);
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.z > ?;");
		chkLine(2, selectFrag, " DELETE FROM AddressCustomerAssoc WHERE  b.leftv IN (SELECT id FROM Address as a WHERE a.z > ?)");
		chkParams(selectFrag, 7, 10, 10);
	}
	@Test
	public void testParentOther6() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[z > 10] {z: 7, cust:null}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
//		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.z > ?;");
//		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.leftv = ? WHERE b.rightv IN (SELECT id FROM Address as a WHERE a.z > ?)");
//		chkParams(selectFrag, 7,10,55, 10);
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.z > ?;");
		chkLine(2, selectFrag, " DELETE FROM CustomerAddressAssoc WHERE  b.rightv IN (SELECT id FROM Address as a WHERE a.z > ?)");
		chkParams(selectFrag, 7, 10, 10);
	}
	

	//---
	private Delia delia;
	private FactoryService factorySvc;
	private DTypeRegistry registry;
	private Runner runner;
	private QueryBuilderService queryBuilderSvc;
	private QueryDetails details = new QueryDetails();
	private boolean useAliasesFlag = true;
	private UpdateFragmentParser fragmentParser;
	private String sqlLine1;
	private String sqlLine2;
	private LogLevel logLevel = LogLevel.DEBUG;
	private String sqlLine3;
	private String sqlLine4;
	private String sqlLine5;

	@Before
	public void init() {
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		delia.getLog().setLevel(logLevel);
		return new DeliaDao(delia);
	}

	private String buildSrcManyToMany() {
		String src = " type Customer struct {id int unique, wid int, relation addr Address optional many } end";
		src += "\n type Address struct {id int unique, z int, relation cust Customer optional many } end";
		src += "\n  insert Customer {id: 55, wid: 33}";
		src += "\n  insert Customer {id: 56, wid: 34}";
		src += "\n  insert Address {id: 100, z:5, cust: [55,56] }";
		return src;
	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}

	private QuerySpec buildQuery(QueryExp exp) {
		QuerySpec spec= queryBuilderSvc.buildSpec(exp, runner);
		return spec;
	}

	private UpdateFragmentParser createFragmentParser(DeliaDao dao, String src, List<TableInfo> tblInfoL) {
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		this.delia = dao.getDelia();
		this.factorySvc = delia.getFactoryService();
		this.registry = dao.getRegistry();
		this.runner = new RunnerImpl(factorySvc, dao.getDbInterface());

		UpdateFragmentParser parser = createParser(dao, tblInfoL); 
		this.queryBuilderSvc = factorySvc.getQueryBuilderService();

		return parser;
	}
	private UpdateFragmentParser createParser(DeliaDao dao) {
		List<TableInfo> tblinfoL = createTblInfoL(); 
		return createParser(dao, tblinfoL);
	}
	private UpdateFragmentParser createParser(DeliaDao dao, List<TableInfo> tblinfoL) {
		SqlHelperFactory sqlHelperFactory = new H2SqlHelperFactory(factorySvc);
		
		WhereFragmentGenerator whereGen = new WhereFragmentGenerator(factorySvc, registry, runner);
		UpdateFragmentParser parser = new UpdateFragmentParser(factorySvc, registry, runner, tblinfoL, dao.getDbInterface(), sqlHelperFactory, whereGen);
		whereGen.tableFragmentMaker = parser;
		return parser;
	}

	private List<TableInfo> createTblInfoL() {
		List<TableInfo> tblinfoL = new ArrayList<>();
		TableInfo info = new  TableInfo("Address", "AddressCustomerAssoc");
		info.tbl1 = "Address";
		info.tbl2 = "Customer";
		//public String fieldName;
		tblinfoL.add(info);
		return tblinfoL;
	}
	private List<TableInfo> createTblInfoLOtherWay() {
		List<TableInfo> tblinfoL = new ArrayList<>();
		TableInfo info = new  TableInfo("Customer", "CustomerAddressAssoc");
		info.tbl1 = "Customer";
		info.tbl2 = "Address";
		//public String fieldName;
		tblinfoL.add(info);
		return tblinfoL;
	}

	private void runAndChk(UpdateStatementFragment selectFrag, String expected) {
		String sql = fragmentParser.renderSelect(selectFrag);
		log.log(sql);
		assertEquals(expected, sql);
	}
	private void runAndChkLine(int lineNum, UpdateStatementFragment selectFrag, String expected) {
		SqlStatementGroup stgroup = fragmentParser.renderUpdateGroup(selectFrag);
		
		//recombine params
		List<DValue> newL = new ArrayList<>();
		for(SqlStatement statement: stgroup.statementL) {
			newL.addAll(statement.paramL);
		}
		stgroup.statementL.get(0).paramL = newL;
		
		String sql = stgroup.flatten(); //fragmentParser.renderSelect(selectFrag);
		log.log(sql);
		if (lineNum == 1) {
			String[] ar = sql.split("\n");
			this.sqlLine1 = ar[0];
			this.sqlLine2 = ar[1];
			if (ar.length > 2) {
				this.sqlLine3 = ar[2];
			}
			if (ar.length > 3) {
				this.sqlLine4 = ar[3];
			}
			if (ar.length > 4) {
				this.sqlLine5 = ar[4];
			}
			assertEquals(expected, sqlLine1);
		}
	}
	private void chkLine(int lineNum, UpdateStatementFragment selectFrag, String expected) {
		if (lineNum == 2) {
			assertEquals(expected, sqlLine2);
		} else if (lineNum == 3) {
			assertEquals(expected, sqlLine3);
		} else if (lineNum == 4) {
			assertEquals(expected, sqlLine4);
		} else if (lineNum == 5) {
			assertEquals(expected, sqlLine5);
		}
	}
	private void chkNoLine(int lineNum) {
		if (lineNum == 2) {
			assertEquals(null, sqlLine2);
		} else if (lineNum == 3) {
			assertEquals(null, sqlLine3);
		} else if (lineNum == 4) {
			assertEquals(null, sqlLine4);
		} else if (lineNum == 5) {
			assertEquals(null, sqlLine5);
		}
	}

	private UpdateStatementFragment buildUpdateFragment(UpdateStatementExp exp, DValue dval) {
		QuerySpec spec= buildQuery((QueryExp) exp.queryExp);
		fragmentParser.useAliases(useAliasesFlag);
		UpdateStatementFragment selectFrag = fragmentParser.parseUpdate(spec, details, dval);
		return selectFrag;
	}

	private UpdateStatementExp buildFromSrc(String src) {
		List<TableInfo> tblinfoL = this.createTblInfoL();
		return buildFromSrc(src, tblinfoL);
	}
	private UpdateStatementExp buildFromSrc(String src, List<TableInfo> tblinfoL) {
		DeliaDao dao = createDao(); 
		Delia xdelia = dao.getDelia();
		xdelia.getOptions().migrationAction = MigrationAction.GENERATE_MIGRATION_PLAN;
		dao.getDbInterface().getCapabilities().setRequiresSchemaMigration(true);
		log.log(src);
		this.fragmentParser = createFragmentParser(dao, src, tblinfoL); 
		
		//		List<Exp> expL = dao.getMostRecentSess().
		DeliaSessionImpl sessImpl = (DeliaSessionImpl) dao.getMostRecentSess();
		UpdateStatementExp updateStatementExp = null;
		for(Exp exp: sessImpl.expL) {
			if (exp instanceof UpdateStatementExp) {
				updateStatementExp = (UpdateStatementExp) exp;
			}
		}
		return updateStatementExp;
	}

	private ConversionResult buildPartialValue(DStructType dtype, DsonExp dsonExp) {
		ConversionResult cres = new ConversionResult();
		cres.localET = new SimpleErrorTracker(log);
		SprigService sprigSvc = new SprigServiceImpl(factorySvc, registry);
		DsonToDValueConverter converter = new DsonToDValueConverter(factorySvc, cres.localET, registry, null, sprigSvc);
		cres.dval = converter.convertOnePartial(dtype.getName(), dsonExp);
		return cres;
	}
	private DValue convertToDVal(UpdateStatementExp updateStatementExp) {
		return convertToDVal(updateStatementExp, "Flight");
	}
	private DValue convertToDVal(UpdateStatementExp updateStatementExp, String typeName) {
		DStructType structType = (DStructType) registry.getType(typeName);
		ConversionResult cres = buildPartialValue(structType, updateStatementExp.dsonExp);
		assertEquals(0, cres.localET.errorCount());
		return cres.dval;
	}
	//these tests use int params (but they could be long,date,...)
	private void chkParams(UpdateStatementFragment selectFrag, Integer...args) {
		StringJoiner joiner = new StringJoiner(",");
		for(DValue dval: selectFrag.statement.paramL) {
			joiner.add(dval.asString());
		}
		log.log("params: " + joiner.toString());
		
		assertEquals(args.length, selectFrag.statement.paramL.size());
		int i = 0;
		for(Integer arg: args) {
			DValue dval = selectFrag.statement.paramL.get(i++);
			assertEquals(arg.intValue(), dval.asInt());
		}
	}

}
