package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.delia.api.Delia;
import org.delia.api.DeliaSessionImpl;
import org.delia.api.MigrationAction;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.dao.DeliaDao;
import org.delia.db.DBAccessContext;
import org.delia.db.SqlHelperFactory;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.sql.fragment.FragmentParserService;
import org.delia.db.sql.fragment.InsertFragmentParser;
import org.delia.db.sql.fragment.InsertStatementFragment;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.db.sql.table.TableInfo;
import org.delia.runner.ConversionResult;
import org.delia.runner.RunnerImpl;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;


public class InsertFragmentParserTests extends FragmentParserTestBase {

	@Test
	public void test1() {
		String src = buildSrc();
		src += "\n insert Flight {field1: 1, field2: 10}";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp);
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChk(selectFrag, "INSERT INTO Flight (field1, field2) VALUES(?, ?)");
		chkParams(selectFrag, "1", "10");
		//		chkNumParams(1, 0, 1);
	}
	@Test
	public void testBadField() {
		String src = buildSrc();
		src += "\n insert Flight {field1: 1, xxxfield2: 10}";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Flight", 1);
		assertEquals(null, dval);
	}
	@Test
	public void test1Null() {
		String src = buildSrcOptional();
		src += "\n insert Flight {field1: 1}";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp);
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChk(selectFrag, "INSERT INTO Flight (field1) VALUES(?)");
		chkParams(selectFrag, "1");
	}
	@Test
	public void testNoPrimaryKey() {
		String src = buildSrcNoPrimaryKey();
		src += "\n insert Flight {field1: 1, field2: 10}";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp);
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChk(selectFrag, "INSERT INTO Flight (field1, field2) VALUES(?, ?)");
		chkParams(selectFrag, "1", "10");
	}
	@Test
	public void testSerial() {
		String src = buildSrcSerial();
		src += "\n insert Flight {}";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp);
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChk(selectFrag, "INSERT INTO Flight DEFAULT VALUES");
		chkParams(selectFrag);
		//		chkNumParams(1, 0, 1);
	}

	
	//------------------- relations ----------------
	@Test
	public void testOneToOne() {
		String src = buildSrcOneToOne();
		src += "\n  insert Customer {id: 55, wid: 33}";
		//		src += "\n  insert Address {id: 100, z:5, cust: 55 }";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Customer");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChk(selectFrag, "INSERT INTO Customer (id, wid) VALUES(?, ?)");
		chkParams(selectFrag, "55", "33");
	}
	@Test
	public void testOneToOneWithChild() {
		String src = buildSrcOneToOne();
		src += "\n  insert Customer {id: 55, wid: 33, addr: 100}";
		//		src += "\n  insert Address {id: 100, z:5, cust: 55 }";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Customer");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChk(selectFrag, "INSERT INTO Customer (id, wid) VALUES(?, ?)");
		chkParams(selectFrag, "55", "33");
	}
	@Test
	public void testOneToOneChild() {
		String src = buildSrcOneToOne();
		src += "\n  insert Customer {id: 55, wid: 33}";
		src += "\n  insert Address {id: 100, z:5, cust: 55 }";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Address");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChk(selectFrag, "INSERT INTO Address (id, z, cust) VALUES(?, ?, ?)");
		chkParams(selectFrag, "100", "5", "55");
	}

	@Test
	public void testOneToManyParent() {
		String src = buildSrcOneToMany();
		src += "\n  insert Customer {id: 55, wid: 33}";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Customer");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChk(selectFrag, "INSERT INTO Customer (id, wid) VALUES(?, ?)");
		chkParams(selectFrag, "55", "33");
	}
	@Test
	public void testOneToManyParentWithChild() {
		String src = buildSrcOneToMany();
		src += "\n  insert Customer {id: 55, wid: 33, addr: 100}";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Customer");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChk(selectFrag, "INSERT INTO Customer (id, wid) VALUES(?, ?)");
		chkParams(selectFrag, "55", "33");
	}
	@Test
	public void testOneToManyParentWithChild2() {
		String src = buildSrcOneToMany();
		src += "\n  insert Customer {id: 55, wid: 33, addr: [100,101]}";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Customer");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChk(selectFrag, "INSERT INTO Customer (id, wid) VALUES(?, ?)");
		chkParams(selectFrag, "55", "33");
	}
	@Test
	public void testOneToManyChild() {
		String src = buildSrcOneToMany();
		src += "\n  insert Customer {id: 55, wid: 33}";
		src += "\n  insert Address {id: 100, z:5, cust: 55 }";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Address");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChk(selectFrag, "INSERT INTO Address (id, z, cust) VALUES(?, ?, ?)");
		chkParams(selectFrag, "100", "5", "55");
	}

	@Test
	public void testManyToManyParentNoAssocTbl() {
		String src = buildSrcManyToMany();
		src += "\n  insert Customer {id: 55, wid: 33}";
		//		src += "\n  insert Customer {id: 56, wid: 34}";
		//		src += "\n  insert Address {id: 100, z:5, cust: [55,56] }";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Customer");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChkLine(1, selectFrag, "INSERT INTO Customer (id, wid) VALUES(?, ?)");
		chkNoLine(2);
		//		chkLine(2, selectFrag, " UPDATE Address as a SET a.z = ? WHERE a.z > ?");
		chkParams(selectFrag, "55", "33");
		chkNumParams(2);
	}
	@Test
	public void testManyToManyParentWithAssocTbl1() {
		String src = buildSrcManyToMany();
		src += "\n  insert Customer {id: 55, wid: 33, addr:100}";
		//		src += "\n  insert Customer {id: 56, wid: 34}";
		//		src += "\n  insert Address {id: 100, z:5, cust: [55,56] }";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Customer");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChkLine(1, selectFrag, "INSERT INTO Customer (id, wid) VALUES(?, ?);");
		chkLine(2, selectFrag, " INSERT INTO AddressCustomerAssoc (leftv, rightv) VALUES(?, ?)");
		chkParams(selectFrag, "55", "33", "100", "55");
		chkNumParams(2, 2);
	}
	@Test
	public void testManyToManyParentWithAssocTbl2() {
		String src = buildSrcManyToMany();
		src += "\n  insert Customer {id: 55, wid: 33, addr:[100,101]}";
		//		src += "\n  insert Customer {id: 56, wid: 34}";
		//		src += "\n  insert Address {id: 100, z:5, cust: [55,56] }";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Customer");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChkLine(1, selectFrag, "INSERT INTO Customer (id, wid) VALUES(?, ?);");
		chkLine(2, selectFrag, " INSERT INTO AddressCustomerAssoc (leftv, rightv) VALUES(?, ?);");
		chkLine(3, selectFrag, " INSERT INTO AddressCustomerAssoc (leftv, rightv) VALUES(?, ?)");
		chkParams(selectFrag, "55", "33", "100", "55", "101", "55");
		chkNumParams(2, 2, 2);
	}

	@Test
	public void testManyToManyChildWithAssocTbl1() {
		String src = buildSrcManyToMany();
		src += "\n  insert Customer {id: 55, wid: 33, addr:100}";
		src += "\n  insert Address {id: 100, z:5, cust: 55 }";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Address");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChkLine(1, selectFrag, "INSERT INTO Address (id, z) VALUES(?, ?);");
		chkLine(2, selectFrag, " INSERT INTO AddressCustomerAssoc (leftv, rightv) VALUES(?, ?)");
		chkParams(selectFrag, "100", "5", "100", "55");
		chkNumParams(2, 2);
	}

	@Test
	public void testManyToManyChildWithAssocTbl2() {
		String src = buildSrcManyToMany();
		src += "\n  insert Customer {id: 55, wid: 33, addr:100}";
		src += "\n  insert Address {id: 100, z:5, cust: [55,56] }";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Address");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChkLine(1, selectFrag, "INSERT INTO Address (id, z) VALUES(?, ?);");
		chkLine(2, selectFrag, " INSERT INTO AddressCustomerAssoc (leftv, rightv) VALUES(?, ?);");
		chkLine(3, selectFrag, " INSERT INTO AddressCustomerAssoc (leftv, rightv) VALUES(?, ?)");
		chkParams(selectFrag, "100", "5", "100", "55", "100", "56");
		chkNumParams(2, 2, 2);
	}


	//---
	private InsertFragmentParser fragmentParser;

	@Before
	public void init() {
	}

	private String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		return src;
	}
	private String buildSrcSerial() {
		String src = "type Flight struct {field1 int primaryKey serial} end";
		return src;
	}
	private String buildSrcOptional() {
		String src = "type Flight struct {field1 int unique, field2 int optional } end";
		return src;
	}
	private String buildSrcNoPrimaryKey() {
		String src = "type Flight struct {field1 int, field2 int } end";
		return src;
	}
	private String buildSrcOneToOne() {
		String src = " type Customer struct {id int unique, wid int, relation addr Address optional one parent } end";
		src += "\n type Address struct {id int unique, z int, relation cust Customer optional one } end";
		return src;
	}
	private String buildSrcOneToMany() {
		String src = " type Customer struct {id int unique, wid int, relation addr Address optional many } end";
		src += "\n type Address struct {id int unique, z int, relation cust Customer optional one } end";
		return src;
	}
	private String buildSrcManyToMany() {
		String src = " type Customer struct {id int unique, wid int, relation addr Address optional many } end";
		src += "\n type Address struct {id int unique, z int, relation cust Customer optional many } end";
		return src;
	}

	private InsertFragmentParser createFragmentParser(DeliaDao dao, String src, List<TableInfo> tblInfoL) {
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		this.delia = dao.getDelia();
		this.factorySvc = delia.getFactoryService();
		this.registry = dao.getRegistry();
		this.runner = new RunnerImpl(factorySvc, dao.getDbInterface());

		InsertFragmentParser parser = createParser(dao, tblInfoL); 
		this.queryBuilderSvc = factorySvc.getQueryBuilderService();

		return parser;
	}
	private InsertFragmentParser createParser(DeliaDao dao) {
		List<TableInfo> tblinfoL = createTblInfoL(); 
		return createParser(dao, tblinfoL);
	}
	private InsertFragmentParser createParser(DeliaDao dao, List<TableInfo> tblinfoL) {
		SqlHelperFactory sqlHelperFactory = new H2SqlHelperFactory(factorySvc);
		DBAccessContext dbctx = new DBAccessContext(runner);

		FragmentParserService fpSvc = new FragmentParserService(factorySvc, registry, runner, tblinfoL, dao.getDbInterface(), dbctx, sqlHelperFactory, null, null);
		InsertFragmentParser parser = new InsertFragmentParser(factorySvc, fpSvc);
		return parser;
	}

	private void runAndChk(InsertStatementFragment selectFrag, String expected) {
		String sql = fragmentParser.renderInsert(selectFrag);
		log.log(sql);
		assertEquals(expected, sql);
	}
	private void runAndChkLine(int lineNum, InsertStatementFragment selectFrag, String expected) {
		SqlStatementGroup stgroup = fragmentParser.renderInsertGroup(selectFrag);
		currentGroup = stgroup;
		for(SqlStatement stat: stgroup.statementL ) {
			numParamL.add(stat.paramL.size());
		}

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
			if (ar.length > 1) {
				this.sqlLine2 = ar[1];
			}
			if (ar.length > 2) {
				this.sqlLine3 = ar[2];
			}
			if (ar.length > 3) {
				this.sqlLine4 = ar[3];
			}
			//			if (ar.length > 4) {
			//				this.sqlLine5 = ar[4];
			//			}
			assertEquals(expected, sqlLine1);
		}
	}
	private void chkLine(int lineNum, InsertStatementFragment selectFrag, String expected) {
		if (lineNum == 2) {
			assertEquals(expected, sqlLine2);
		} else if (lineNum == 3) {
			assertEquals(expected, sqlLine3);
		} else if (lineNum == 4) {
			assertEquals(expected, sqlLine4);
			//		} else if (lineNum == 5) {
			//			assertEquals(expected, sqlLine5);
		}
	}

	private InsertStatementFragment buildInsertFragment(InsertStatementExp exp, DValue dval) {
		//		fragmentParser.useAliases(useAliasesFlag);
		InsertStatementFragment selectFrag = fragmentParser.parseInsert(exp.typeName, dval);
		return selectFrag;
	}

	private InsertStatementExp buildFromSrc(String src) {
		List<TableInfo> tblinfoL = this.createTblInfoL();
		return buildFromSrc(src, tblinfoL);
	}
	private InsertStatementExp buildFromSrc(String src, List<TableInfo> tblinfoL) {
		DeliaDao dao = createDao(); 
		Delia xdelia = dao.getDelia();
		xdelia.getOptions().migrationAction = MigrationAction.GENERATE_MIGRATION_PLAN;
		dao.getDbInterface().getCapabilities().setRequiresSchemaMigration(true);
		log.log(src);
		this.fragmentParser = createFragmentParser(dao, src, tblinfoL); 

		//		List<Exp> expL = dao.getMostRecentSess().
		DeliaSessionImpl sessImpl = (DeliaSessionImpl) dao.getMostRecentSession();
		InsertStatementExp insertStatementExp = null;
		for(Exp exp: sessImpl.expL) {
			if (exp instanceof InsertStatementExp) {
				insertStatementExp = (InsertStatementExp) exp;
			}
		}
		return insertStatementExp;
	}

	private DValue convertToDVal(InsertStatementExp insertStatementExp) {
		return convertToDVal(insertStatementExp, "Flight", 0);
	}
	private DValue convertToDVal(InsertStatementExp insertStatementExp, String typeName) {
		return convertToDVal(insertStatementExp, typeName, 0);
	}
	private DValue convertToDVal(InsertStatementExp insertStatementExp, String typeName, int expectedErrorCount) {
		DStructType structType = (DStructType) registry.getType(typeName);
		ConversionResult cres = buildPartialValue(structType, insertStatementExp.dsonExp);
		assertEquals(expectedErrorCount, cres.localET.errorCount());
		return cres.dval;
	}

	//these tests use int params (but they could be long,date,...)
	private void chkParams(InsertStatementFragment selectFrag, String...args) {
		StringJoiner joiner = new StringJoiner(",");

		SqlStatement stat;
		stat = selectFrag.statement;

		for(DValue dval: stat.paramL) {
			String ss = convertToString(dval);
			joiner.add(ss);
		}
		log.log("params: " + joiner.toString());

		assertEquals(args.length, stat.paramL.size());
		int i = 0;
		for(String arg: args) {
			DValue dval = stat.paramL.get(i++);
			String ss = convertToString(dval);
			assertEquals(arg, ss);
		}
	}
}
