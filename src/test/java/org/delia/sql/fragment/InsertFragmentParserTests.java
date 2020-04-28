package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
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
import org.delia.db.sql.fragment.AssocTableReplacer;
import org.delia.db.sql.fragment.FieldFragment;
import org.delia.db.sql.fragment.FragmentHelper;
import org.delia.db.sql.fragment.InsertStatementFragment;
import org.delia.db.sql.fragment.SelectFragmentParser;
import org.delia.db.sql.fragment.SqlFragment;
import org.delia.db.sql.fragment.StatementFragmentBase;
import org.delia.db.sql.fragment.TableFragment;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.db.sql.prepared.TableInfoHelper;
import org.delia.db.sql.table.TableInfo;
import org.delia.error.SimpleErrorTracker;
import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationManyRule;
import org.delia.runner.ConversionResult;
import org.delia.runner.DsonToDValueConverter;
import org.delia.runner.Runner;
import org.delia.runner.RunnerImpl;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigServiceImpl;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.junit.Before;
import org.junit.Test;


public class InsertFragmentParserTests extends NewBDDBase {
	
	//single use!!!
	public static class InsertFragmentParser extends SelectFragmentParser {

		private boolean useAliases = false;
		private AssocTableReplacer assocTblReplacer;

		public InsertFragmentParser(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator, List<TableInfo> tblinfoL, DBInterface dbInterface, 
				SqlHelperFactory sqlHelperFactory, WhereFragmentGenerator whereGen, AssocTableReplacer assocTblReplacer) {
			super(factorySvc, registry, varEvaluator, tblinfoL, dbInterface, sqlHelperFactory, whereGen);
			this.assocTblReplacer = assocTblReplacer;
		}

		public InsertStatementFragment parseInsert(String typeName, QueryDetails details, DValue dval) {
			InsertStatementFragment insertFrag = new InsertStatementFragment();

			Map<String, DRelation> mmMap = new HashMap<>();

			//init tbl
			DStructType structType = getMainType(typeName); 
			TableFragment tblFrag = createTable(structType, insertFrag);
			insertFrag.tblFrag = tblFrag;

			generateSetFields(structType, insertFrag, dval, mmMap);
			generateAssocUpdateIfNeeded(structType, insertFrag, dval, mmMap);

//			fixupForParentFields(structType, selectFrag);

			if (! useAliases) {
				removeAllAliases(insertFrag);
				if (insertFrag.assocUpdateFrag != null) {
					removeAllAliases(insertFrag.assocUpdateFrag);
				}
			}

			return insertFrag;
		}

		/**
		 * Postgres doesn't like alias in UPDATE statements
		 * @param insertFrag
		 */
		private void removeAllAliases(InsertStatementFragment insertFrag) {
			for(FieldFragment ff: insertFrag.fieldL) {
				ff.alias = null;
			}
			insertFrag.tblFrag.alias = null;
		}

		private void generateSetFields(DStructType structType, InsertStatementFragment insertFrag,
				DValue dval, Map<String, DRelation> mmMap) {
			//we assume partialVal same type as structType!! (or maybe a base class)

			int index = insertFrag.fieldL.size(); //setValuesL is parallel array to fieldL
			if (index != 0) {

				log.log("WHY FILLING INNNNNNNNN");
				for(int i = 0; i < index; i++) {
					insertFrag.setValuesL.add("????");
				}
				DeliaExceptionHelper.throwError("unexpeced-fields-in-insert", "should not occur");
			}

			for(TypePair pair: structType.getAllFields()) {
				if (pair.type.isStructShape()) {
					if (! shouldGenerateFKConstraint(pair, structType)) {
						continue;
					}
					if (DRuleHelper.isManyToManyRelation(pair, structType)) {
						DValue inner = dval.asStruct().getField(pair.name);
						if (inner == null) {
							mmMap.put(pair.name, null);
						} else {
							mmMap.put(pair.name, inner.asRelation());
						}
						continue;
					}
				}

				DValue inner = dval.asMap().get(pair.name);
				if (inner == null) {
					continue;
				}

				DValue dvalToUse = inner;
				if (inner.getType().isRelationShape()) {
					DRelation drel = inner.asRelation();
					dvalToUse  = drel.getForeignKey(); //TODO; handle composite keys later
				}

				FieldFragment ff = FragmentHelper.buildFieldFrag(structType, insertFrag, pair);
				insertFrag.setValuesL.add("?");
				insertFrag.fieldL.add(ff);
				insertFrag.statement.paramL.add(dvalToUse);

				index++;
			}
		}
		private boolean shouldGenerateFKConstraint(TypePair pair, DStructType dtype) {
			//key goes in child only
			RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, pair);
			if (info != null && !info.isParent) {
				return true;
			}
			return false;
		}
		private void generateAssocUpdateIfNeeded(DStructType structType,
				InsertStatementFragment insertFrag, DValue dval, Map<String, DRelation> mmMap) {
			if (mmMap.isEmpty()) {
				return;
			}

			for(String fieldName: mmMap.keySet()) {
				RelationManyRule ruleMany = DRuleHelper.findManyRule(structType, fieldName);
				if (ruleMany != null) {
					RelationInfo info = ruleMany.relInfo;
					insertFrag.assocUpdateFrag = new InsertStatementFragment();
					genAssocField(insertFrag, insertFrag.assocUpdateFrag, structType, dval, mmMap, fieldName, info,  
							insertFrag.statement);
				}
			}
		}

		private void genAssocField(InsertStatementFragment insertFrag, InsertStatementFragment assocInsertFrag, DStructType structType, DValue mainDVal, Map<String, DRelation> mmMap, String fieldName, 
				RelationInfo info, SqlStatement statement) {
			//update assoctabl set leftv=x where rightv=y
			TableInfo tblinfo = TableInfoHelper.findTableInfoAssoc(this.tblinfoL, info.nearType, info.farType);
			assocInsertFrag.tblFrag = this.createAssocTable(assocInsertFrag, tblinfo.assocTblName);

			//struct is Address AddressCustomerAssoc
			String field1;
			String field2;
			TypePair keyPair1;
			TypePair keyPair2;
			if (tblinfo.tbl1.equalsIgnoreCase(structType.getName())) {
				field1 = "rightv";
				field2 = "leftv";
				keyPair1 = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
				keyPair2 = DValueHelper.findPrimaryKeyFieldPair(info.farType);
				DRelation drel = mmMap.get(fieldName);
				for(DValue dval: drel.getMultipleKeys()) {
					FieldFragment ff = FragmentHelper.buildFieldFragForTable(assocInsertFrag.tblFrag, insertFrag, keyPair1);
					insertFrag.setValuesL.add("?");
					insertFrag.fieldL.add(ff);
					insertFrag.statement.paramL.add(mainDVal);
					
					ff = FragmentHelper.buildFieldFragForTable(assocInsertFrag.tblFrag, insertFrag, keyPair2);
					insertFrag.setValuesL.add("?");
					insertFrag.fieldL.add(ff);
					insertFrag.statement.paramL.add(dval);
				}
			} else {
				field1 = "leftv";
				field2 = "rightv";
				keyPair1 = DValueHelper.findPrimaryKeyFieldPair(info.farType);
				keyPair2 = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
			}

		}

//		protected void buildAssocTblUpdate(InsertStatementFragment assocUpdateFrag, DStructType structType, Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName, SqlStatement statement) {
//			DRelation drel = mmMap.get(fieldName); //100
//			DValue dvalToUse  = drel.getForeignKey(); //TODO; handle composite keys later
//
//			RelationInfo farInfo = DRuleHelper.findOtherSideMany(info.farType, structType);
//			TypePair pair2 = DValueHelper.findField(farInfo.nearType, farInfo.fieldName);
//			TypePair rightPair = new TypePair(assocFieldName, pair2.type);
//			FieldFragment ff = FragmentHelper.buildFieldFragForTable(assocUpdateFrag.tblFrag, assocUpdateFrag, rightPair);
//			statement.paramL.add(dvalToUse);
//			assocUpdateFrag.setValuesL.add("?");
//			assocUpdateFrag.fieldL.add(ff);
//		}
//


		public String renderInsert(InsertStatementFragment selectFrag) {
			if(selectFrag.setValuesL.isEmpty()) {
				selectFrag.statement.sql = ""; //nothing to do
				return selectFrag.statement.sql;
			}

			selectFrag.statement.sql = selectFrag.render();
			return selectFrag.statement.sql;
		}
		
		public SqlStatementGroup renderInsertGroup(InsertStatementFragment selectFrag) {
			SqlStatementGroup stgroup = new SqlStatementGroup();
			if(selectFrag.setValuesL.isEmpty()) {
				selectFrag.statement.sql = ""; //nothing to do
				return stgroup;
			}

			SqlStatement mainStatement = selectFrag.statement;
			mainStatement.sql = selectFrag.render();
			List<DValue> save = new ArrayList<>(mainStatement.paramL); //copy
			
			mainStatement.paramL.clear();
			stgroup.add(selectFrag.statement);
			initMainParams(mainStatement, save, selectFrag.assocUpdateFrag);
			initMainParams(mainStatement, save, selectFrag.assocDeleteFrag);
			initMainParams(mainStatement, save, selectFrag.assocMergeInfoFrag);
			if (mainStatement.paramL.isEmpty()) {
				mainStatement.paramL.addAll(save);
				return stgroup; //no inner frags
			}
			
			addIfNotNull(stgroup, selectFrag.assocUpdateFrag, save, nextStartIndex(selectFrag.assocDeleteFrag, selectFrag.assocMergeInfoFrag));
			addIfNotNull(stgroup, selectFrag.assocDeleteFrag, save, nextStartIndex(selectFrag.assocMergeInfoFrag));
			addIfNotNull(stgroup, selectFrag.assocMergeInfoFrag, save, Integer.MAX_VALUE);
			
//			if (selectFrag.doUpdateLast) {
//				SqlStatement stat = stgroup.statementL.remove(0); //move first to last
//				List<DValue> firstParams = stat.paramL;
//				stgroup.statementL.add(stat);
//				
////				//swap param lists too
////				SqlStatement newFirst = stgroup.statementL.get(0);
////				List<DValue> tmp = stat.paramL;
////				newFirst.paramL = firstParams;
////				stat.paramL = tmp;
//			}
//			
			return stgroup;
		}
		private int nextStartIndex(StatementFragmentBase... frags) {
			for(StatementFragmentBase frag: frags) {
				if (frag != null) {
					return frag.paramStartIndex;
				}
			}
			return Integer.MAX_VALUE;
		}

		private void initMainParams(SqlStatement mainStatement, List<DValue> saveL, StatementFragmentBase innerFrag) {
			if (! mainStatement.paramL.isEmpty()) {
				return;
			}
			
			if (innerFrag != null) {
				for(int i = 0; i < innerFrag.paramStartIndex; i++) {
					DValue dval = saveL.get(i);
					mainStatement.paramL.add(dval);
				}
			}
		}

		private void addIfNotNull(SqlStatementGroup stgroup, StatementFragmentBase innerFrag, List<DValue> paramL, int nextStartIndex) {
			if (innerFrag != null) {
				SqlStatement stat = innerFrag.statement;
				stat.sql = innerFrag.render();
				//we are copying more than needed, but that's ok
				int n = nextStartIndex < paramL.size() ? nextStartIndex : paramL.size();
				for(int i = innerFrag.paramStartIndex; i < n; i++) {
					DValue dval = paramL.get(i);
					stat.paramL.add(dval);
				}
				stgroup.add(stat);
			}
		}
	}	
	
	@Test
	public void test1() {
		String src = buildSrc();
		src += "\n insert Flight {field1: 1, field2: 10}";
		
		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp);
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 
		
		runAndChk(selectFrag, "INSERT Flight (field1, field2) VALUES(?, ?)");
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
		
		runAndChk(selectFrag, "INSERT Flight (field1) VALUES(?)");
		chkParams(selectFrag, "1");
	}
	@Test
	public void testNoPrimaryKey() {
		String src = buildSrcNoPrimaryKey();
		src += "\n insert Flight {field1: 1, field2: 10}";
		
		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp);
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 
		
		runAndChk(selectFrag, "INSERT Flight (field1, field2) VALUES(?, ?)");
		chkParams(selectFrag, "1", "10");
	}
	
	@Test
	public void testOneToOne() {
		String src = buildSrcOneToOne();
		src += "\n  insert Customer {id: 55, wid: 33}";
//		src += "\n  insert Address {id: 100, z:5, cust: 55 }";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Customer");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 
		
		runAndChk(selectFrag, "INSERT Customer (id, wid) VALUES(?, ?)");
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
		
		runAndChk(selectFrag, "INSERT Customer (id, wid) VALUES(?, ?)");
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
		
		runAndChk(selectFrag, "INSERT Address (id, z, cust) VALUES(?, ?, ?)");
		chkParams(selectFrag, "100", "5", "55");
	}

	@Test
	public void testOneToManyParent() {
		String src = buildSrcOneToMany();
		src += "\n  insert Customer {id: 55, wid: 33}";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Customer");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChk(selectFrag, "INSERT Customer (id, wid) VALUES(?, ?)");
		chkParams(selectFrag, "55", "33");
	}
	@Test
	public void testOneToManyParentWithChild() {
		String src = buildSrcOneToMany();
		src += "\n  insert Customer {id: 55, wid: 33, addr: 100}";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Customer");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChk(selectFrag, "INSERT Customer (id, wid) VALUES(?, ?)");
		chkParams(selectFrag, "55", "33");
	}
	@Test
	public void testOneToManyParentWithChild2() {
		String src = buildSrcOneToMany();
		src += "\n  insert Customer {id: 55, wid: 33, addr: [100,101]}";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Customer");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChk(selectFrag, "INSERT Customer (id, wid) VALUES(?, ?)");
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

		runAndChk(selectFrag, "INSERT Address (id, z, cust) VALUES(?, ?, ?)");
		chkParams(selectFrag, "100", "5", "55");
	}
	
	@Test
	public void testManyToManyParent() {
		String src = buildSrcManyToMany();
		src += "\n  insert Customer {id: 55, wid: 33}";
//		src += "\n  insert Customer {id: 56, wid: 34}";
//		src += "\n  insert Address {id: 100, z:5, cust: [55,56] }";

		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp, "Customer");
		InsertStatementFragment selectFrag = buildInsertFragment(insertStatementExp, dval); 

		runAndChk(selectFrag, "INSERT Customer (id, wid) VALUES(?, ?)");
		chkParams(selectFrag, "55", "33");
	}
//	@Test
//	public void testManyToManyChild() {
//		String src = buildSrcOneToMany();
//		src += "\n  insert Customer {id: 55, wid: 33}";
//		src += "\n  insert Address {id: 100, z:5, cust: 55 }";
//
//		InsertStatementExp insertStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(insertStatementExp, "Address");
//		InsertStatementFragment selectFrag = buildUpdateFragment(insertStatementExp, dval); 
//
//		runAndChk(selectFrag, "INSERT Address (id, z, cust) VALUES(?, ?, ?)");
//		chkParams(selectFrag, "100", "5", "55");
//	}
	
	
	
	//---
	private Delia delia;
	private FactoryService factorySvc;
	private DTypeRegistry registry;
	private Runner runner;
	private QueryBuilderService queryBuilderSvc;
	private QueryDetails details = new QueryDetails();
	private InsertFragmentParser fragmentParser;
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

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}

	private QuerySpec buildQuery(QueryExp exp) {
		QuerySpec spec= queryBuilderSvc.buildSpec(exp, runner);
		return spec;
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
		
		WhereFragmentGenerator whereGen = new WhereFragmentGenerator(factorySvc, registry, runner);
	    AssocTableReplacer assocTblReplacer = new AssocTableReplacer(factorySvc, registry, runner, tblinfoL, dao.getDbInterface(), sqlHelperFactory, whereGen);
		InsertFragmentParser parser = new InsertFragmentParser(factorySvc, registry, runner, tblinfoL, dao.getDbInterface(), sqlHelperFactory, whereGen, assocTblReplacer);
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

	private void runAndChk(InsertStatementFragment selectFrag, String expected) {
		String sql = fragmentParser.renderInsert(selectFrag);
		log.log(sql);
		assertEquals(expected, sql);
	}
	private void runAndChkLine(int lineNum, InsertStatementFragment selectFrag, String expected) {
		String sql = fragmentParser.renderInsert(selectFrag);
		log.log(sql);
		if (lineNum == 1) {
			String[] ar = sql.split("\n");
			this.sqlLine1 = ar[0];
			this.sqlLine2 = ar[1];
			assertEquals(expected, sqlLine1);
		}
	}
	private void chkLine(int lineNum, InsertStatementFragment selectFrag, String expected) {
		if (lineNum == 2) {
			assertEquals(expected, sqlLine2);
		}
	}

	private InsertStatementFragment buildInsertFragment(InsertStatementExp exp, DValue dval) {
//		fragmentParser.useAliases(useAliasesFlag);
		InsertStatementFragment selectFrag = fragmentParser.parseInsert(exp.typeName, details, dval);
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
		DeliaSessionImpl sessImpl = (DeliaSessionImpl) dao.getMostRecentSess();
		InsertStatementExp insertStatementExp = null;
		for(Exp exp: sessImpl.expL) {
			if (exp instanceof InsertStatementExp) {
				insertStatementExp = (InsertStatementExp) exp;
			}
		}
		return insertStatementExp;
	}

	private ConversionResult buildPartialValue(DStructType dtype, DsonExp dsonExp) {
		ConversionResult cres = new ConversionResult();
		cres.localET = new SimpleErrorTracker(log);
		SprigService sprigSvc = new SprigServiceImpl(factorySvc, registry);
		DsonToDValueConverter converter = new DsonToDValueConverter(factorySvc, cres.localET, registry, null, sprigSvc);
		cres.dval = converter.convertOnePartial(dtype.getName(), dsonExp);
		return cres;
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
			joiner.add(dval.asString());
		}
		log.log("params: " + joiner.toString());
		
		assertEquals(args.length, stat.paramL.size());
		int i = 0;
		for(String arg: args) {
			DValue dval = stat.paramL.get(i++);
			assertEquals(arg, dval.asString());
		}
	}
}
