package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

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
import org.delia.db.sql.table.TableInfo;
import org.delia.error.SimpleErrorTracker;
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
	public void testManyToMany() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[55] {wid: 333}";

		UpdateStatementExp updateStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChk(selectFrag, "UPDATE Customer as a SET a.wid = 333 WHERE a.id = ?");
	}
	@Test
	public void testManyToManyParentAll() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[true] {wid: 333, addr:100}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = 333;");
		chkLine(2, selectFrag, "UPDATE AddressCustomerAssoc as b SET b.leftv = 100");
	}
	@Test
	public void testManyToManyParentAllOtherWay() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[true] {wid: 333, addr:100}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = 333;");
		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.rightv = 100");
	}
	@Test
	public void testManyToManyParentAll3() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[true] {z: 7, cust:55}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = 7;");
		chkLine(2, selectFrag, "UPDATE AddressCustomerAssoc as b SET b.rightv = 55");
	}
	@Test
	public void testManyToManyParentAll4() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[true] {z: 7, cust:55}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = 7;");
		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.leftv = 55");
	}
	
	
	//scenario 2: ID-----------------------------
	@Test
	public void testManyToManyParentId() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[55] {wid: 333, addr:100}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = 333 WHERE a.id = ?;");
		chkLine(2, selectFrag, "UPDATE AddressCustomerAssoc as b SET b.leftv = 100 WHERE b.rightv = ?");
	}
	@Test
	public void testManyToManyParentIdOtherWay() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[55] {wid: 333, addr:100}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = 333 WHERE a.id = ?;");
		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.rightv = 100 WHERE b.leftv = ?");
	}
	@Test
	public void testManyToManyParentId3() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[100] {z: 7, cust:55}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = 7 WHERE a.id = ?;");
		chkLine(2, selectFrag, "UPDATE AddressCustomerAssoc as b SET b.rightv = 55 WHERE b.leftv = ?");
	}
	@Test
	public void testManyToManyParentId4() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[100] {z: 7, cust:55}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = 7 WHERE a.id = ?;");
		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.leftv = 55 WHERE b.rightv = ?");
	}

	//scenario 3: OTHER -----------------------------
	@Test
	public void testManyToManyParentOther() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[wid > 10] {wid: 333, addr:100}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = 333 WHERE a.id = ?;");
		chkLine(2, selectFrag, "UPDATE AddressCustomerAssoc as b SET b.leftv = 100 WHERE b.rightv = ?");
	}
	@Test
	public void testManyToManyParentOtherOtherWay() {
		String src = buildSrcManyToMany();
		src += "\n  update Customer[wid > 10] {wid: 333, addr:100}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Customer");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChkLine(1, selectFrag, "UPDATE Customer as a SET a.wid = 333 WHERE a.id = ?;");
		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.rightv = 100 WHERE b.leftv = ?");
	}
	@Test
	public void testManyToManyParentOther3() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[z > 10] {z: 7, cust:55}";

		List<TableInfo> tblinfoL = createTblInfoL();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = 7 WHERE a.id = ?;");
		chkLine(2, selectFrag, "UPDATE AddressCustomerAssoc as b SET b.rightv = 55 WHERE b.leftv = ?");
	}
	@Test
	public void testManyToManyParentOther4() {
		String src = buildSrcManyToMany();
		src += "\n  update Address[z > 10] {z: 7, cust:55}";

		List<TableInfo> tblinfoL = createTblInfoLOtherWay();
		UpdateStatementExp updateStatementExp = buildFromSrc(src, tblinfoL);
		DValue dval = convertToDVal(updateStatementExp, "Address");
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChkLine(1, selectFrag, "UPDATE Address as a SET a.z = 7 WHERE a.id = ?;");
		chkLine(2, selectFrag, "UPDATE CustomerAddressAssoc as b SET b.leftv = 55 WHERE b.rightv = ?");
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


	@Before
	public void init() {
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}

	private String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
	private String buildSrcNoPrimaryKey() {
		String src = "type Flight struct {field1 int, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
	private String buildSrcOneToOne() {
		String src = " type Customer struct {id int unique, wid int, relation addr Address optional one parent } end";
		src += "\n type Address struct {id int unique, z int, relation cust Customer optional one } end";
		src += "\n  insert Customer {id: 55, wid: 33}";
		src += "\n  insert Address {id: 100, z:5, cust: 55 }";
		return src;
	}
	private String buildSrcOneToMany() {
		String src = " type Customer struct {id int unique, wid int, relation addr Address optional many } end";
		src += "\n type Address struct {id int unique, z int, relation cust Customer optional one } end";
		src += "\n  insert Customer {id: 55, wid: 33}";
		src += "\n  insert Address {id: 100, z:5, cust: 55 }";
		src += "\n  insert Address {id: 101, z:6, cust: 55 }";
		return src;
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
		String sql = fragmentParser.renderSelect(selectFrag);
		log.log(sql);
		if (lineNum == 1) {
			String[] ar = sql.split("\n");
			this.sqlLine1 = ar[0];
			this.sqlLine2 = ar[1];
			assertEquals(expected, sqlLine1);
		}
	}
	private void chkLine(int lineNum, UpdateStatementFragment selectFrag, String expected) {
		if (lineNum == 2) {
			assertEquals(expected, sqlLine2);
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

}
