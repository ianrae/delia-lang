package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.delia.api.Delia;
import org.delia.api.DeliaSessionImpl;
import org.delia.api.MigrationAction;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.dao.DeliaDao;
import org.delia.db.DBAccessContext;
import org.delia.db.DBInterface;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.SqlHelperFactory;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.postgres.PostgresAssocTablerReplacer;
import org.delia.db.sql.fragment.FragmentParserService;
import org.delia.db.sql.fragment.UpdateFragmentParser;
import org.delia.db.sql.fragment.UpdateStatementFragment;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.db.sql.table.TableInfo;
import org.delia.log.LogLevel;
import org.delia.runner.ConversionResult;
import org.delia.runner.DeliaException;
import org.delia.runner.RunnerImpl;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;


public class AssocCrudTests extends FragmentParserTestBase {
	
//	//=============== one-to-one ======================
	@Test(expected=DeliaException.class)
	public void testOneToOne() {
		String src = buildSrcOneToOne();
		src += "\n  update Customer[true] {wid: 333, insert addr:100}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ?;");
		chkLine(2, selectFrag, " DELETE FROM AddressCustomerAssoc;");
		chkLine(3, selectFrag, " WITH cte1 AS (SELECT ? as leftv, id as rightv FROM Customer) INSERT INTO AddressCustomerAssoc as t SELECT * from cte1");
		chkNoLine(4);
		chkParams(selectFrag, 333,  100);
		chkNumParams(1, 0, 1);
	}
	
//	//=============== one-to-many ======================
	@Test(expected=DeliaException.class)
	public void testOneToMany() {
		String src = buildSrcManyToOne();
		src += "\n  update Customer[true] {wid: 333, insert addr:100}";
		src += "\n  update Address[true] {z:5, cust:100}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ?;");
		chkLine(2, selectFrag, " DELETE FROM AddressCustomerAssoc;");
		chkLine(3, selectFrag, " WITH cte1 AS (SELECT ? as leftv, id as rightv FROM Customer) INSERT INTO AddressCustomerAssoc as t SELECT * from cte1");
		chkNoLine(4);
		chkParams(selectFrag, 333,  100);
		chkNumParams(1, 0, 1);
	}

//	//=============== many to many ======================
	@Test(expected=DeliaException.class)
	public void testAll() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[true] {wid: 333, insert addr:100}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ?;");
		chkLine(2, selectFrag, " DELETE FROM AddressCustomerAssoc;");
		chkLine(3, selectFrag, " WITH cte1 AS (SELECT ? as leftv, id as rightv FROM Customer) INSERT INTO AddressCustomerAssoc as t SELECT * from cte1");
		chkNoLine(4);
		chkParams(selectFrag, 333,  100);
		chkNumParams(1, 0, 1);
	}
	
	//scenario 2: ID-----------------------------
	@Test
	public void testId() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[55] {wid: 333, insert addr:100}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 

		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " INSERT INTO AddressCustomerAssoc as b (b.leftv, b.rightv) VALUES(?, ?)");
		chkNoLine(3);
		chkParams(selectFrag, 333,55, 100,55);
		chkNumParams(2, 2);
	}
	@Test
	public void testId2() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[55] {wid: 333, insert addr:[100,101]}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 

		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " INSERT INTO AddressCustomerAssoc as b (b.leftv, b.rightv) VALUES(?, ?);");
		chkLine(3, selectFrag, " INSERT INTO AddressCustomerAssoc as b (b.leftv, b.rightv) VALUES(?, ?)");
		chkNoLine(4);
		chkParams(selectFrag, 333,55, 100,55, 101,55);
		chkNumParams(2, 2, 2);
	}
	@Test
	public void testId2OtherWash() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[55] {wid: 333, insert addr:[100,101]}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 

		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " INSERT INTO CustomerAddressAssoc as b (b.leftv, b.rightv) VALUES(?, ?);");
		chkLine(3, selectFrag, " INSERT INTO CustomerAddressAssoc as b (b.leftv, b.rightv) VALUES(?, ?)");
		chkNoLine(4);
		chkParams(selectFrag, 333,55, 55,100, 55,101);
		chkNumParams(2, 2, 2);
	}
	
	@Test
	public void testId3() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[100] {z: 7, insert cust:55}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
		
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " INSERT INTO AddressCustomerAssoc as b (b.leftv, b.rightv) VALUES(?, ?)");
		chkNoLine(3);
		chkParams(selectFrag, 7, 100, 100,55);
		chkNumParams(2, 2);
	}
	@Test
	public void testId3OtherWay() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[100] {z: 7, insert cust:55}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
		
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " INSERT INTO CustomerAddressAssoc as b (b.leftv, b.rightv) VALUES(?, ?)");
		chkNoLine(3);
		chkParams(selectFrag, 7, 100, 55,100);
		chkNumParams(2, 2);
	}
	@Test(expected=DeliaException.class)
	public void testId5() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[55] {wid: 333, insert addr:null}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 

		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " INSERT INTO AddressCustomerAssoc as b (b.leftv, b.rightv) VALUES(?, ?)");
		chkNoLine(3);
		chkParams(selectFrag, 333,55, 100,55);
		chkNumParams(2, 2);
	}
	

	//scenario 3: OTHER -----------------------------
	@Test(expected=DeliaException.class)
	public void testOther() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[wid > 10 and id < 500] {wid: 333, insert addr:100}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
		
		runAndChkLine(1, selectFrag, "DELETE FROM AddressCustomerAssoc WHERE  rightv IN (SELECT id FROM Customer as a WHERE  a.wid > ? and  a.id < ?);");
		chkLine(2, selectFrag, " WITH cte1 AS (SELECT ? as leftv, id as rightv FROM Customer as a WHERE  a.wid > ? and  a.id < ?) INSERT INTO AddressCustomerAssoc as t SELECT * from cte1;");
		chkLine(3, selectFrag, " UPDATE Customer as a SET a.wid = ? WHERE  a.wid > ? and  a.id < ?");
		chkNoLine(4);
		chkParams(selectFrag, 10,500, 100,10,500, 333,10,500);
		chkNumParams(2, 3, 3);
	}
	
	
	//==================== assocCrud delete -----------------------------
	@Test
	public void testIdDelete() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[55] {wid: 333, delete addr:100}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 

		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " DELETE FROM AddressCustomerAssoc as b WHERE leftv = ? and right == ?");
		chkNoLine(3);
		chkParams(selectFrag, 333,55, 100,55);
		chkNumParams(2, 2);
	}
	@Test
	public void testId2Delete() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[55] {wid: 333, insert addr:[100,101]}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 

		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " INSERT INTO AddressCustomerAssoc as b (b.leftv, b.rightv) VALUES(?, ?);");
		chkLine(3, selectFrag, " INSERT INTO AddressCustomerAssoc as b (b.leftv, b.rightv) VALUES(?, ?)");
		chkNoLine(4);
		chkParams(selectFrag, 333,55, 100,55, 101,55);
		chkNumParams(2, 2, 2);
	}
	@Test
	public void testId2OtherWayDelete() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[55] {wid: 333, insert addr:[100,101]}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 

		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " INSERT INTO CustomerAddressAssoc as b (b.leftv, b.rightv) VALUES(?, ?);");
		chkLine(3, selectFrag, " INSERT INTO CustomerAddressAssoc as b (b.leftv, b.rightv) VALUES(?, ?)");
		chkNoLine(4);
		chkParams(selectFrag, 333,55, 55,100, 55,101);
		chkNumParams(2, 2, 2);
	}
	
	@Test
	public void testId3Delete() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[100] {z: 7, insert cust:55}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
		
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " INSERT INTO AddressCustomerAssoc as b (b.leftv, b.rightv) VALUES(?, ?)");
		chkNoLine(3);
		chkParams(selectFrag, 7, 100, 100,55);
		chkNumParams(2, 2);
	}
	@Test
	public void testId3OtherWayDelete() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[100] {z: 7, insert cust:55}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 
		
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " INSERT INTO CustomerAddressAssoc as b (b.leftv, b.rightv) VALUES(?, ?)");
		chkNoLine(3);
		chkParams(selectFrag, 7, 100, 55,100);
		chkNumParams(2, 2);
	}
	@Test(expected=DeliaException.class)
	public void testId5Delete() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[55] {wid: 333, insert addr:null}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval, recentCres.assocCrudMap); 

		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?;");
		chkLine(2, selectFrag, " INSERT INTO AddressCustomerAssoc as b (b.leftv, b.rightv) VALUES(?, ?)");
		chkNoLine(3);
		chkParams(selectFrag, 333,55, 100,55);
		chkNumParams(2, 2);
	}
	
	

	//---
	private QueryDetails details = new QueryDetails();
	private boolean useAliasesFlag = true;
	private UpdateFragmentParser fragmentParser;
	private LogLevel logLevel = LogLevel.DEBUG;
	private ConversionResult recentCres;


	@Before
	public void init() {
	}

	private String buildSrcOneToOne() {
		String src = " type Customer struct {id int unique, wid int, relation addr Address optional one } end";
		src += "\n type Address struct {id int unique, z int, relation cust Customer optional one } end";
		return src;
	}
	private String buildSrcManyToOne() {
		String src = " type Customer struct {id int unique, wid int, relation addr Address optional many } end";
		src += "\n type Address struct {id int unique, z int, relation cust Customer optional one } end";
		return src;
	}
	private String buildSrcManyToMany() {
		String src = " type Customer struct {id int unique, wid int, relation addr Address optional many } end";
		src += "\n type Address struct {id int unique, z int, relation cust Customer optional many } end";
		return src;
	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
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
		DBAccessContext dbctx = new DBAccessContext(runner);
		FragmentParserService fpSvc = new FragmentParserService(factorySvc, registry, runner, tblinfoL, dao.getDbInterface(), dbctx, sqlHelperFactory, whereGen);
		PostgresAssocTablerReplacer assocTblReplacer = new PostgresAssocTablerReplacer(factorySvc, fpSvc);
		UpdateFragmentParser parser = new UpdateFragmentParser(factorySvc, fpSvc, assocTblReplacer);
		whereGen.tableFragmentMaker = parser;
		return parser;
	}

	private void runAndChk(UpdateStatementFragment selectFrag, String expected) {
		String sql = fragmentParser.renderSelect(selectFrag);
		log.log(sql);
		assertEquals(expected, sql);
	}
	private void runAndChkLine(int lineNum, UpdateStatementFragment selectFrag, String expected) {
		SqlStatementGroup stgroup = fragmentParser.renderUpdateGroup(selectFrag);
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

	private UpdateStatementFragment buildUpdateFragment(UpdateStatementExp exp, DValue dval, Map<String, String> assocCrudMap) {
		QuerySpec spec= buildQuery((QueryExp) exp.queryExp);
		fragmentParser.useAliases(useAliasesFlag);
		UpdateStatementFragment selectFrag = fragmentParser.parseUpdate(spec, details, dval, assocCrudMap);
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

	private DValue convertToDVal(UpdateStatementExp updateStatementExp, String typeName) {
		DStructType structType = (DStructType) registry.getType(typeName);
		ConversionResult cres = buildPartialValue(structType, updateStatementExp.dsonExp);
		this.recentCres = cres;
		assertEquals(0, cres.localET.errorCount());
		return cres.dval;
	}
	//these tests use int params (but they could be long,date,...)
	private void chkParams(UpdateStatementFragment selectFrag, Integer...args) {
		StringJoiner joiner = new StringJoiner(",");
		
		SqlStatement stat;
		if (selectFrag.doUpdateLast) {
//			int n = currentGroup.statementL.size();
			stat = currentGroup.statementL.get(0);
		} else {
			stat = selectFrag.statement;
		}
		
		for(DValue dval: stat.paramL) {
			joiner.add(dval.asString());
		}
		log.log("params: " + joiner.toString());
		
		assertEquals(args.length, stat.paramL.size());
		int i = 0;
		for(Integer arg: args) {
			DValue dval = stat.paramL.get(i++);
			assertEquals(arg.intValue(), dval.asInt());
		}
	}

}
