//package org.delia.sql.fragment;
//
//
//import static org.junit.Assert.assertEquals;
//
//import java.util.List;
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
//import org.delia.db.sql.fragment.AssocTableReplacer;
//import org.delia.db.sql.fragment.FragmentParserService;
//import org.delia.db.sql.fragment.UpdateFragmentParser;
//import org.delia.db.sql.fragment.UpdateStatementFragment;
//import org.delia.db.sql.fragment.WhereFragmentGenerator;
//import org.delia.db.sql.table.TableInfo;
//import org.delia.runner.ConversionResult;
//import org.delia.runner.RunnerImpl;
//import org.delia.type.DStructType;
//import org.delia.type.DValue;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//
//public class UpdateFragmentParserTests extends FragmentParserTestBase {
//
//	@Test
//	public void testPrimaryKey() {
//		String src = buildSrc();
//		src += " update Flight[1] {field2: 111}";
//		
//		UpdateStatementExp updateStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(updateStatementExp);
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = ? WHERE a.field1 = ?");
//	}
//	
//	@Test
//	public void testPrimaryKeyNoAlias() {
//		String src = buildSrc();
//		src += " update Flight[1] {field2: 111}";
//		
//		UpdateStatementExp updateStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(updateStatementExp);
//		useAliasesFlag = false;
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Flight SET field2 = ? WHERE field1 = ?");
//	}
//
//	@Test
//	public void testAllRows() {
//		String src = buildSrc();
//		src += " update Flight[true] {field2: 111}";
//		
//		UpdateStatementExp updateStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(updateStatementExp);
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = ?");
//	}
//
//	@Test
//	public void testOp() {
//		String src = buildSrc();
//		src += " update Flight[field1 > 0] {field2: 111}";
//		
//		UpdateStatementExp updateStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(updateStatementExp);
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = ? WHERE a.field1 > ?");
//	}
//	@Test
//	public void testOpNoPrimaryKey() {
//		String src = buildSrcNoPrimaryKey();
//		src += " update Flight[field1 > 0] {field2: 111}";
//		
//		UpdateStatementExp updateStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(updateStatementExp);
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = ? WHERE a.field1 > ?");
//	}
//
//	@Test
//	public void testBasic() {
//		String src = buildSrc();
//		src += " update Flight[1] {field2: 111}";
//		
//		UpdateStatementExp updateStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(updateStatementExp);
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = ? WHERE a.field1 = ?");
//	}
//	
//	@Test
//	public void testOneToOne() {
//		String src = buildSrcOneToOne();
//		src += "\n  update Customer[55] {wid: 333}";
//
//		UpdateStatementExp updateStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?");
//	}
//	@Test
//	public void testOneToOneParent() {
//		String src = buildSrcOneToOne();
//		src += "\n  update Customer[55] {wid: 333, addr:100}";
//
//		UpdateStatementExp updateStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?");
//	}
//	@Test
//	public void testOneToOneChild() {
//		String src = buildSrcOneToOne();
//		src += "\n  update Address[100] {z: 6, cust:55}";
//
//		UpdateStatementExp updateStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(updateStatementExp, "Address");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Address as a SET a.cust = ?, a.z = ? WHERE a.id = ?");
//	}
//	
//	@Test
//	public void testOneToMany() {
//		String src = buildSrcOneToMany();
//		src += "\n  update Customer[55] {wid: 333}";
//
//		UpdateStatementExp updateStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?");
//	}
//	@Test
//	public void testOneToManyParent() {
//		String src = buildSrcOneToMany();
//		src += "\n  update Customer[55] {wid: 333, addr:100}";
//
//		UpdateStatementExp updateStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(updateStatementExp, "Customer");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?");
//	}
//	@Test
//	public void testOneToManyChild() {
//		String src = buildSrcOneToMany();
//		src += "\n  update Address[100] {z: 6, cust:55}";
//
//		UpdateStatementExp updateStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(updateStatementExp, "Address");
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Address as a SET a.cust = ?, a.z = ? WHERE a.id = ?");
//	}
//	
//	
////TODO: support orderBy
////	@Test
////	public void testOrderBy() {
////		String src = buildSrc();
////		src += " let x = Flight[true].orderBy('field2')";
////		UpdateStatementFragment selectFrag = buildUpdateFragment(src); 
////
////		runAndChk(selectFrag, "SELECT * FROM Flight as a ORDER BY a.field2");
////	}
//	
//	//TODO: support limit
////	@Test
////	public void testOrderByLimit() {
////		String src = buildSrc();
////		src += " update Flight[true].limit(4) {field2: 111}";
////		
////		UpdateStatementExp updateStatementExp = buildFromSrc(src);
////		DValue dval = convertToDVal(updateStatementExp);
////		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
////		
////		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = 111");
////	}
//
//
//	//TODO: support relations
////	@Test
////	public void test11Relation() {
////		String src = buildSrcOneToOne();
////		UpdateStatementFragment selectFrag = buildUpdateFragment(src); 
////
////		//[1] SQL:             SELECT a.id,b.id as addr FROM Customer as a LEFT JOIN Address as b ON b.cust=a.id  WHERE  a.id=?;  -- (55)
////		runAndChk(selectFrag, "SELECT * FROM Customer as a WHERE  a.id = ?");
////		chkParamInt(selectFrag.statement, 1, 55);
////	}
//
//	//---
//	private QueryDetails details = new QueryDetails();
//	private UpdateFragmentParser fragmentParser;
//	private boolean useAliasesFlag = true;
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
//	private String buildSrc() {
//		String src = "type Flight struct {field1 int unique, field2 int } end";
//		src += "\n insert Flight {field1: 1, field2: 10}";
//		src += "\n insert Flight {field1: 2, field2: 20}";
//		return src;
//	}
//	private String buildSrcNoPrimaryKey() {
//		String src = "type Flight struct {field1 int, field2 int } end";
//		src += "\n insert Flight {field1: 1, field2: 10}";
//		src += "\n insert Flight {field1: 2, field2: 20}";
//		return src;
//	}
//	private String buildSrcOneToOne() {
//		String src = " type Customer struct {id int unique, wid int, relation addr Address optional one parent } end";
//		src += "\n type Address struct {id int unique, z int, relation cust Customer optional one } end";
//		src += "\n  insert Customer {id: 55, wid: 33}";
//		src += "\n  insert Address {id: 100, z:5, cust: 55 }";
//		return src;
//	}
//	private String buildSrcOneToMany() {
//		String src = " type Customer struct {id int unique, wid int, relation addr Address optional many } end";
//		src += "\n type Address struct {id int unique, z int, relation cust Customer optional one } end";
//		src += "\n  insert Customer {id: 55, wid: 33}";
//		src += "\n  insert Address {id: 100, z:5, cust: 55 }";
//		src += "\n  insert Address {id: 101, z:6, cust: 55 }";
//		return src;
//	}
//	private String buildSrcManyToMany() {
//		String src = " type Customer struct {id int unique, wid int, relation addr Address optional many } end";
//		src += "\n type Address struct {id int unique, z int, relation cust Customer optional many } end";
//		src += "\n  insert Customer {id: 55, wid: 33}";
//		src += "\n  insert Customer {id: 56, wid: 34}";
//		src += "\n  insert Address {id: 100, z:5, cust: [55,56] }";
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
//	private UpdateFragmentParser createParser(DeliaGenericDao dao) {
//		List<TableInfo> tblinfoL = createTblInfoL(); 
//		return createParser(dao, tblinfoL);
//	}
//	private UpdateFragmentParser createParser(DeliaGenericDao dao, List<TableInfo> tblinfoL) {
//		
//		WhereFragmentGenerator whereGen = new WhereFragmentGenerator(factorySvc, registry, runner, null);
//		FragmentParserService fpSvc = createFragmentParserService(whereGen, dao, tblinfoL);
//	    AssocTableReplacer assocTblReplacer = new AssocTableReplacer(factorySvc, fpSvc);
//		UpdateFragmentParser parser = new UpdateFragmentParser(factorySvc, fpSvc, assocTblReplacer);
//		whereGen.tableFragmentMaker = parser;
//		return parser;
//	}
//
//	private void runAndChk(UpdateStatementFragment selectFrag, String expected) {
//		String sql = fragmentParser.renderSelect(selectFrag);
//		log.log(sql);
//		assertEquals(expected, sql);
//	}
//
//	private UpdateStatementFragment buildUpdateFragment(UpdateStatementExp exp, DValue dval) {
//		QuerySpec spec= buildQuery((QueryExp) exp.queryExp);
//		fragmentParser.useAliases(useAliasesFlag);
//		UpdateStatementFragment selectFrag = fragmentParser.parseUpdate(spec, details, dval, recentCres.assocCrudMap);
//		return selectFrag;
//	}
//
//	private UpdateStatementExp buildFromSrc(String src) {
//		List<TableInfo> tblinfoL = this.createTblInfoL();
//		return buildFromSrc(src, tblinfoL);
//	}
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
//	private DValue convertToDVal(UpdateStatementExp updateStatementExp) {
//		return convertToDVal(updateStatementExp, "Flight");
//	}
//	private DValue convertToDVal(UpdateStatementExp updateStatementExp, String typeName) {
//		DStructType structType = (DStructType) registry.getType(typeName);
//		ConversionResult cres = buildPartialValue(structType, updateStatementExp.dsonExp);
//		assertEquals(0, cres.localET.errorCount());
//		this.recentCres = cres;
//		return cres.dval;
//	}
//
//}
