package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.delia.api.Delia;
import org.delia.api.DeliaSessionImpl;
import org.delia.api.MigrationAction;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaDao;
import org.delia.db.DBAccessContext;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.SqlHelperFactory;
import org.delia.db.TableExistenceService;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.fragment.AliasedFragment;
import org.delia.db.sql.fragment.FieldFragment;
import org.delia.db.sql.fragment.FragmentHelper;
import org.delia.db.sql.fragment.JoinFragment;
import org.delia.db.sql.fragment.LimitFragment;
import org.delia.db.sql.fragment.OffsetFragment;
import org.delia.db.sql.fragment.OrderByFragment;
import org.delia.db.sql.fragment.SqlFragment;
import org.delia.db.sql.fragment.TableFragment;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.ListWalker;
import org.delia.db.sql.table.TableInfo;
import org.delia.runner.Runner;
import org.delia.runner.RunnerImpl;
import org.delia.runner.VarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;


public class FragmentParserTests extends NewBDDBase {
	
	public static class SelectStatementFragment implements SqlFragment {
		public SqlStatement statement = new SqlStatement();
		public Map<String,TableFragment> aliasMap = new HashMap<>();
		
		public List<SqlFragment> earlyL = new ArrayList<>();
		public List<FieldFragment> fieldL = new ArrayList<>();
		public TableFragment tblFrag;
		public JoinFragment joinFrag; //TODO later a list
		public List<SqlFragment> whereL = new ArrayList<>();
		public OrderByFragment orderByFrag = null;
		public LimitFragment limitFrag = null;
		public OffsetFragment offsetFrag = null;
		
		@Override
		public String render() {
			StrCreator sc = new StrCreator();
			sc.o("SELECT ");
			renderEarly(sc);
			renderFields(sc);
			sc.o(" FROM %s", tblFrag.render());
			renderIfPresent(sc, joinFrag);
			
			if (! whereL.isEmpty()) {
				sc.o(" WHERE ");
				renderWhereL(sc);
			}
			
			renderIfPresent(sc, orderByFrag);
			renderIfPresent(sc, limitFrag);
			renderIfPresent(sc, offsetFrag);
			return sc.str;
		}


		private void renderIfPresent(StrCreator sc, SqlFragment frag) {
			if (frag != null) {
				sc.o(frag.render());
			}
		}


		private void renderEarly(StrCreator sc) {
			for(SqlFragment frag: earlyL) {
				String s = frag.render();
				sc.o(s);
			}
		}

		private void renderWhereL(StrCreator sc) {
			for(SqlFragment frag: whereL) {
				String s = frag.render();
				sc.o(s);
			}
		}


		private void renderFields(StrCreator sc) {
			ListWalker<FieldFragment> walker = new ListWalker<>(fieldL);
			while(walker.hasNext()) {
				FieldFragment fieldF = walker.next();
				sc.o(fieldF.render());
				walker.addIfNotLast(sc, ",");
			}
		}


	}
	
	public static class FragmentParser extends ServiceBase {
		private int nextAliasIndex = 0;
		private QueryTypeDetector queryDetectorSvc;
		private DTypeRegistry registry;
//		private ScalarValueBuilder dvalBuilder;
//		private ValueHelper valueHelper;
//		private VarEvaluator varEvaluator;
		private WhereFragmentGenerator whereGen;
		private SelectFuncHelper selectFnHelper;
		private TableExistenceServiceImpl existSvc;
		private FKHelper fkHelper;
		
		public FragmentParser(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator, DBInterface dbInterface, SqlHelperFactory sqlHelperFactory) {
			super(factorySvc);
			this.registry = registry;
			this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
			
//			this.dvalBuilder = factorySvc.createScalarValueBuilder(registry);
//			this.whereConverter = new SqlWhereConverter(factorySvc, registry, queryDetectorSvc);
//			this.filterRunner = new FilterFnRunner(registry);
//			this.valueHelper = new ValueHelper(factorySvc);
//			this.varEvaluator = varEvaluator;
			this.whereGen = new WhereFragmentGenerator(factorySvc, registry, varEvaluator);
//			this.selectFnHelper = new SelectFuncHelper(new DBAccessContext(registry, varEvaluator));
			this.selectFnHelper = new SelectFuncHelper(factorySvc, registry);
			this.existSvc = new TableExistenceServiceImpl(dbInterface, new DBAccessContext(registry, varEvaluator));
			
			List<TableInfo> tblinfoL = new ArrayList<>();
			this.fkHelper = new FKHelper(factorySvc, registry, tblinfoL, sqlHelperFactory, varEvaluator, existSvc);
		}
		
		public void createAlias(AliasedFragment frag) {
			char ch = (char) ('a' + nextAliasIndex++);
			frag.alias = String.format("%c", ch);
		}

		public SelectStatementFragment parseSelect(QuerySpec spec, QueryDetails details) {
			SelectStatementFragment selectFrag = new SelectStatementFragment();
			
			//init tbl
			DStructType structType = getMainType(spec); 
			TableFragment tblFrag = createTable(structType, selectFrag);
			selectFrag.tblFrag = tblFrag;
			
			initFields(spec, structType, selectFrag);
			addJoins(spec, structType, selectFrag, details);
			addFns(spec, structType, selectFrag);

			generateQueryFns(spec, structType, selectFrag);
			
			if (selectFrag.fieldL.isEmpty()) {
				FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); //new FieldFragment();
				selectFrag.fieldL.add(fieldF);
			}
			
			return selectFrag;
		}
		
		private void addJoins(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag, QueryDetails details) {
			fkHelper.generateFKsQuery(spec, details, structType, selectFrag, this);
		}

		public TableFragment createTable(DStructType structType, SelectStatementFragment selectFrag) {
			TableFragment tblFrag = new TableFragment();
			tblFrag.structType = structType;
			createAlias(tblFrag);
			tblFrag.name = structType.getName();
			selectFrag.aliasMap.put(tblFrag.name, tblFrag);
			return tblFrag;
		}
		public TableFragment createAssocTable(SelectStatementFragment selectFrag, String tableName) {
			TableFragment tblFrag = new TableFragment();
			tblFrag.structType = null;
			createAlias(tblFrag);
			tblFrag.name = tableName;
			selectFrag.aliasMap.put(tblFrag.name, tblFrag);
			return tblFrag;
		}
		
		public void generateQueryFns(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			this.doOrderByIfPresent(spec, structType, selectFrag);
			this.doLimitIfPresent(spec, structType, selectFrag);
			this.doOffsetIfPresent(spec, structType, selectFrag);
		}
		
		private void doOrderByIfPresent(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			QueryFuncExp qfexp = selectFnHelper.findFn(spec, "orderBy");
			if (qfexp == null) {
				return;
			}
			
			StringJoiner joiner = new StringJoiner(",");
			boolean isDesc = false;
			for(Exp exp : qfexp.argL) {
				if (exp instanceof IdentExp) {
					isDesc = exp.strValue().equals("desc");
				} else {
					String fieldName = exp.strValue();
					if (fieldName.contains(".")) {
						fieldName = StringUtils.substringAfter(fieldName, ".");
					}
					if (! DValueHelper.fieldExists(structType, fieldName)) {
						DeliaExceptionHelper.throwError("unknown-field", "type '%s' does not have field '%s'. Invalid orderBy parameter", structType.getName(), fieldName);
					}
					
					String alias = FragmentHelper.findAlias(structType, selectFrag);
					joiner.add(String.format("%s.%s", alias, fieldName));
				}
			}

			String asc = isDesc ? "desc" : null;
			OrderByFragment frag = FragmentHelper.buildRawOrderByFrag(structType, joiner.toString(), asc, selectFrag);
			OrderByFragment orderByFrag = selectFrag.orderByFrag;
			if (orderByFrag == null) {
				selectFrag.orderByFrag = frag;
			} else {
				OrderByFragment tmp = selectFrag.orderByFrag; //swap
				selectFrag.orderByFrag = frag;
				selectFrag.orderByFrag.additionalL.add(tmp);
			}
		}
		private void doLimitIfPresent(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			QueryFuncExp qfexp = selectFnHelper.findFn(spec, "limit");
			if (qfexp == null) {
				return;
			}
			IntegerExp exp = (IntegerExp) qfexp.argL.get(0);
			Integer n = exp.val;

			LimitFragment frag = new LimitFragment(n);
			selectFrag.limitFrag = frag;
		}
		private void doOffsetIfPresent(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			QueryFuncExp qfexp = selectFnHelper.findFn(spec, "offset");
			if (qfexp == null) {
				return;
			}
			IntegerExp exp = (IntegerExp) qfexp.argL.get(0);
			Integer n = exp.val;

			OffsetFragment frag = new OffsetFragment(n);
			selectFrag.offsetFrag = frag;
		}


		private void addFns(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			//TODO: for now we implement exist using count(*). improve later
			if (selectFnHelper.isCountPresent(spec) || selectFnHelper.isExistsPresent(spec)) {
				String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "count");
				if (fieldName == null) {
					FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); //new FieldFragment();
					fieldF.fnName = "COUNT";
					selectFrag.fieldL.add(fieldF);
				} else {
					addFnField("COUNT", fieldName, structType, selectFrag);
				}
			} else if (selectFnHelper.isMinPresent(spec)) {
				String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "min");
				addFnField("MIN", fieldName, structType, selectFrag);
			} else if (selectFnHelper.isMaxPresent(spec)) {
				String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "max");
				addFnField("MAX", fieldName, structType, selectFrag);
			} else if (selectFnHelper.isFirstPresent(spec)) {
				String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "first");
				AliasedFragment top = FragmentHelper.buildAliasedFrag(null, "TOP 1 ");
				selectFrag.earlyL.add(top);
				if (fieldName == null) {
					FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); 
					selectFrag.fieldL.add(fieldF);
				} else {
					FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, fieldName);
					selectFrag.fieldL.add(fieldF);
				}
			} else if (selectFnHelper.isLastPresent(spec)) {
				String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "last");
				AliasedFragment top = FragmentHelper.buildAliasedFrag(null, "TOP 1 ");
				selectFrag.earlyL.add(top);
				if (fieldName == null) {
					forceAddOrderByPrimaryKey(structType, selectFrag, "desc");
				} else {
					FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, fieldName);
					selectFrag.fieldL.add(fieldF);
					forceAddOrderByField(structType, fieldName, "desc", selectFrag);
				}
			} else {
//				sc.o("SELECT * FROM %s", typeName);
			}
		}

		private void forceAddOrderByPrimaryKey(DStructType structType, SelectStatementFragment selectFrag, String asc) {
			TypePair pair = DValueHelper.findPrimaryKeyFieldPair(structType);
			if (pair == null) {
				return; //no primary key
			}
			forceAddOrderByField(structType, pair.name, asc, selectFrag);
		}
		private void forceAddOrderByField(DStructType structType, String fieldName, String asc, SelectStatementFragment selectFrag) {
			OrderByFragment orderByFrag = FragmentHelper.buildOrderByFrag(structType, fieldName, asc, selectFrag);
			selectFrag.orderByFrag = orderByFrag;
		}

		private void addFnField(String fnName, String fieldName, DStructType structType, SelectStatementFragment selectFrag) {
			FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, fieldName);
			if (fieldF == null) {
				DeliaExceptionHelper.throwError("unknown-field", "Field %s.%s unknown in %s function", structType.getName(), fieldName, fnName);
			}
			fieldF.fnName = fnName;
			addOrReplace(selectFrag, fieldF);
		}

		private void addOrReplace(SelectStatementFragment selectFrag, FieldFragment fieldF) {
			selectFrag.fieldL.add(fieldF);
			// TODO Auto-generated method stub
			
		}

		private void initFields(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			
			QueryType queryType = queryDetectorSvc.detectQueryType(spec);
			switch(queryType) {
			case ALL_ROWS:
			{
//				addWhereExist(sc, spec);
			}
				break;
			case OP:
//				addWhereClauseOp(sc, spec, typeName, tbl, statement);
				whereGen.addWhereClauseOp(spec, structType, selectFrag);
				break;
			case PRIMARY_KEY:
			default:
			{
//				addWhereClausePrimaryKey(sc, spec, spec.queryExp.filter, typeName, tbl, statement);
				whereGen.addWhereClausePrimaryKey(spec, spec.queryExp.filter, structType, selectFrag);
			}
				break;
			}
		}

		private DStructType getMainType(QuerySpec spec) {
			DStructType structType = (DStructType) registry.findTypeOrSchemaVersionType(spec.queryExp.typeName);
			return structType;
		}

		private FieldFragment buildStarFieldFrag(DStructType structType, SelectStatementFragment selectFrag) {
			TypePair pair = DValueHelper.findPrimaryKeyFieldPair(structType);
			if (pair == null) {
				FieldFragment fieldF = FragmentHelper.buildEmptyFieldFrag(structType, selectFrag);
				fieldF.isStar = true;
				return fieldF;
			}
			FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, pair);
			fieldF.isStar = true;
			return fieldF;
		}
		
//		private FieldFragment buildFieldFrag(DStructType structType, SelectStatementFragment selectFrag, TypePair pair) {
//			return FragmentHelper.buildFieldFrag(structType, selectFrag, pair);
//		}

		public String render(SelectStatementFragment selectFrag) {
			return selectFrag.render();
		}
	}


	@Test
	public void testPrimaryKey() {
		String src = buildSrc();
		FragmentParser parser = createFragmentParser(src); 
		
		QuerySpec spec= buildPrimaryKeyQuery("Flight", 1);
		SelectStatementFragment selectFrag = parser.parseSelect(spec, details);
		
		String sql = parser.render(selectFrag);
		log.log(sql);
		assertEquals("SELECT * FROM Flight as a WHERE a.field1 = ?", sql);
	}
	
	@Test
	public void testAllRows() {
		String src = buildSrc();
		FragmentParser parser = createFragmentParser(src); 
		
		QuerySpec spec= buildAllRowsQuery("Flight");
		SelectStatementFragment selectFrag = parser.parseSelect(spec, details);
		
		String sql = parser.render(selectFrag);
		assertEquals("SELECT * FROM Flight as a", sql);
	}
	
	@Test
	public void testOp() {
		String src = buildSrc();
		fragmentParser = createFragmentParser(src); 
		
		QuerySpec spec= buildOpQuery("Flight", "field2", 10);
		SelectStatementFragment selectFrag = fragmentParser.parseSelect(spec, details);
		
		runAndChk(selectFrag, "SELECT * FROM Flight as a WHERE a.field2 = ?");
	}
	
	@Test
	public void testMax() {
		String src = buildSrc();
		src += " let x = Flight[1].field1.max()";
		SelectStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT MAX(a.field1) FROM Flight as a WHERE a.field1 = ?");
	}
	
	@Test
	public void testMin() {
		String src = buildSrc();
		src += " let x = Flight[1].field1.min()";
		SelectStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT MIN(a.field1) FROM Flight as a WHERE a.field1 = ?");
	}
	@Test
	public void testCount() {
		String src = buildSrc();
		src += " let x = Flight[true].count()";
		SelectStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT COUNT(*) FROM Flight as a");
	}
	@Test
	public void testCountField() {
		String src = buildSrc();
		src += " let x = Flight[true].field1.count()";
		SelectStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT COUNT(a.field1) FROM Flight as a");
	}
	
	@Test
	public void testFirst() {
		String src = buildSrc();
		src += " let x = Flight[true].first()";
		SelectStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT TOP 1 * FROM Flight as a");
	}
	@Test
	public void testFirstField() {
		String src = buildSrc();
		src += " let x = Flight[true].field1.first()";
		SelectStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT TOP 1 a.field1 FROM Flight as a");
	}
	
	@Test
	public void testLast() {
		String src = buildSrc();
		src += " let x = Flight[true].last()";
		SelectStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT TOP 1 * FROM Flight as a ORDER BY a.field1 desc");
	}
	@Test
	public void testLastField() {
		String src = buildSrc();
		src += " let x = Flight[true].field1.last()";
		SelectStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT TOP 1 a.field1 FROM Flight as a ORDER BY a.field1 desc");
	}
	@Test
	public void testLastFieldOrderBy() {
		String src = buildSrc();
		src += " let x = Flight[true].orderBy('field2').field1.last()";
		SelectStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT TOP 1 a.field1 FROM Flight as a ORDER BY a.field2, a.field1 desc");
	}
	
	
	@Test
	public void testLastNoPrimaryKey() {
		String src = buildSrcNoPrimaryKey();
		src += " let x = Flight[true].last()";
		SelectStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT TOP 1 * FROM Flight as a");
	}
	@Test
	public void testLastNoPrimaryKeyField() {
		String src = buildSrcNoPrimaryKey();
		src += " let x = Flight[true].field2.last()";
		SelectStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT TOP 1 a.field2 FROM Flight as a ORDER BY a.field2 desc");
	}
	
	@Test
	public void testOrderBy() {
		String src = buildSrc();
		src += " let x = Flight[true].orderBy('field2')";
		SelectStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT * FROM Flight as a ORDER BY a.field2");
	}
	@Test
	public void testOrderByLimitOffset() {
		String src = buildSrc();
		src += " let x = Flight[true].orderBy('field2').limit(4).offset(10)";
		SelectStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT * FROM Flight as a ORDER BY a.field2 LIMIT 4 OFFSET 10");
	}
	
	@Test
	public void test11Relation() {
		String src = buildSrcOneToOne();
		SelectStatementFragment selectFrag = buildSelectFragment(src); 

		//[1] SQL:             SELECT a.id,b.id as addr FROM Customer as a LEFT JOIN Address as b ON b.cust=a.id  WHERE  a.id=?;  -- (55)
		runAndChk(selectFrag, "SELECT a.id,b.id as addr FROM Customer as a LEFT JOIN Address as b ON b.cust=a.id WHERE a.id = ?");
	}

	//---
	private Delia delia;
	private FactoryService factorySvc;
	private DTypeRegistry registry;
	private Runner runner;
	private QueryBuilderService queryBuilderSvc;
	private ScalarValueBuilder builder;
	private QueryDetails details = new QueryDetails();

	private FragmentParser fragmentParser;
	

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
		String src = " type Customer struct {id int unique, relation addr Address optional one parent } end";
		src += "\n type Address struct {id int unique, relation cust Customer optional one } end";
		src += "\n  insert Customer {id: 55 }";
		src += "\n  insert Address {id: 100, cust: 55 }";
		src += "\n  let x = Customer[55]";
		return src;
	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}
	
	private QuerySpec buildPrimaryKeyQuery(String typeName, int id) {
		DValue dval = builder.buildInt(id);
		QueryExp exp = queryBuilderSvc.createPrimaryKeyQuery(typeName, dval);
		QuerySpec spec= queryBuilderSvc.buildSpec(exp, runner);
		return spec;
	}
	private QuerySpec buildAllRowsQuery(String typeName) {
		QueryExp exp = queryBuilderSvc.createAllRowsQuery(typeName);
		QuerySpec spec= queryBuilderSvc.buildSpec(exp, runner);
		return spec;
	}
	private QuerySpec buildOpQuery(String typeName, String fieldName, int wid) {
		DValue dval = builder.buildInt(wid);
		QueryExp exp = queryBuilderSvc.createEqQuery(typeName, fieldName, dval);
		QuerySpec spec= queryBuilderSvc.buildSpec(exp, runner);
		return spec;
	}
	private QuerySpec buildQuery(QueryExp exp) {
		QuerySpec spec= queryBuilderSvc.buildSpec(exp, runner);
		return spec;
	}

	private FragmentParser createFragmentParser(String src) {
		DeliaDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);
		
		this.delia = dao.getDelia();
		this.factorySvc = delia.getFactoryService();
		this.registry = dao.getRegistry();
		this.runner = new RunnerImpl(factorySvc, dao.getDbInterface());
		
		SqlHelperFactory sqlHelperFactory = new H2SqlHelperFactory(factorySvc);
		FragmentParser parser = new FragmentParser(factorySvc, registry, runner, dao.getDbInterface(), sqlHelperFactory);
		
		this.queryBuilderSvc = factorySvc.getQueryBuilderService();
		this.builder = factorySvc.createScalarValueBuilder(registry);
		
		return parser;
	}

	private FragmentParser createFragmentParser(DeliaDao dao, String src) {
		boolean b = dao.initialize(src);
		assertEquals(true, b);
		
		this.delia = dao.getDelia();
		this.factorySvc = delia.getFactoryService();
		this.registry = dao.getRegistry();
		this.runner = new RunnerImpl(factorySvc, dao.getDbInterface());
		
		SqlHelperFactory sqlHelperFactory = new H2SqlHelperFactory(factorySvc);
		FragmentParser parser = new FragmentParser(factorySvc, registry, runner, dao.getDbInterface(), sqlHelperFactory);
		
		this.queryBuilderSvc = factorySvc.getQueryBuilderService();
		this.builder = factorySvc.createScalarValueBuilder(registry);
		
		return parser;
	}
	private void runAndChk(SelectStatementFragment selectFrag, String expected) {
		String sql = fragmentParser.render(selectFrag);
		log.log(sql);
		assertEquals(expected, sql);
	}

	private SelectStatementFragment buildSelectFragment(String src) {
		LetStatementExp letStatementExp = buildFromSrc(src);
		QuerySpec spec= buildQuery((QueryExp) letStatementExp.value);
		SelectStatementFragment selectFrag = fragmentParser.parseSelect(spec, details);
		return selectFrag;
	}

	private LetStatementExp buildFromSrc(String src) {
		DeliaDao dao = createDao(); 
		Delia xdelia = dao.getDelia();
		xdelia.getOptions().migrationAction = MigrationAction.GENERATE_MIGRATION_PLAN;
		dao.getDbInterface().getCapabilities().setRequiresSchemaMigration(true);
		this.fragmentParser = createFragmentParser(dao, src); 
		
//		List<Exp> expL = dao.getMostRecentSess().
		DeliaSessionImpl sessImpl = (DeliaSessionImpl) dao.getMostRecentSess();
		LetStatementExp letStatementExp = null;
		for(Exp exp: sessImpl.expL) {
			if (exp instanceof LetStatementExp) {
				letStatementExp = (LetStatementExp) exp;
			}
		}
		return letStatementExp;
	}


}
