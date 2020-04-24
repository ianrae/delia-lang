package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.delia.api.Delia;
import org.delia.api.DeliaSessionImpl;
import org.delia.api.MigrationAction;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaDao;
import org.delia.db.DBAccessContext;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.QueryBuilderService;
import org.delia.db.QuerySpec;
import org.delia.db.ValueHelper;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.ListWalker;
import org.delia.runner.Runner;
import org.delia.runner.RunnerImpl;
import org.delia.runner.VarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;


public class FragmentParserTests extends NewBDDBase {
	
	public static interface SqlFragment {
		String render();
	}
	
	public static class AliasedFragment implements SqlFragment {
		public String alias;
		public String name;
		
		@Override
		public String render() {
			if (StringUtils.isEmpty(alias)) {
				return name;
			}
			return String.format("%s.%s", alias, name);
		}
	}
	public static class TableFragment extends AliasedFragment {
		@Override
		public String render() {
			if (StringUtils.isEmpty(alias)) {
				return name;
			}
			return String.format("%s as %s", name, alias);
		}
	}
	
	public static class FieldFragment extends AliasedFragment {
		public DStructType structType;
		public DType fieldType;
		public boolean isStar;		
		public String fnName;

		@Override
		public String render() {
			if (isStar) {
				if (fnName != null) {
					return String.format("%s(*)", fnName);
				}
				return "*";
			} else if (fnName != null) {
				return String.format("%s(%s)", fnName, super.render());
			}
			
			return super.render();
		}
	}
	
	public static class OpFragment implements SqlFragment {
		public AliasedFragment left;
		public AliasedFragment right;
		public String op;
		
		public OpFragment(String op) {
			this.op = op;
		}
		
		@Override
		public String render() {
			String s = String.format("%s %s %s", left.render(), op, right.render());
			return s;
		}
	}
	
	public static class SelectStatementFragment implements SqlFragment {
		public List<FieldFragment> fieldL = new ArrayList<>();
		public TableFragment tblFrag;
		public Map<String,TableFragment> aliasMap = new HashMap<>();
		public SqlStatement statement = new SqlStatement();
		public List<SqlFragment> whereL = new ArrayList<>();
		
		@Override
		public String render() {
			StrCreator sc = new StrCreator();
			sc.o("SELECT ");
			renderFields(sc);
			sc.o(" FROM %s", tblFrag.render());
			if (! whereL.isEmpty()) {
				sc.o(" WHERE ");
				renderWhereL(sc);
			}
			return sc.str;
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
		private ScalarValueBuilder dvalBuilder;
		private ValueHelper valueHelper;
		private VarEvaluator varEvaluator;
		private WhereFragmentGenerator whereGen;
		private SelectFuncHelper selectFnHelper;
		
		public FragmentParser(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator) {
			super(factorySvc);
			this.registry = registry;
			this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
			
			this.dvalBuilder = factorySvc.createScalarValueBuilder(registry);
//			this.whereConverter = new SqlWhereConverter(factorySvc, registry, queryDetectorSvc);
//			this.filterRunner = new FilterFnRunner(registry);
			this.valueHelper = new ValueHelper(factorySvc);
			this.varEvaluator = varEvaluator;
			this.whereGen = new WhereFragmentGenerator(factorySvc, registry, varEvaluator);
//			this.selectFnHelper = new SelectFuncHelper(new DBAccessContext(registry, varEvaluator));
			this.selectFnHelper = new SelectFuncHelper(factorySvc, registry);
		}
		
		public void createAlias(AliasedFragment frag) {
			char ch = (char) ('a' + nextAliasIndex++);
			frag.alias = String.format("%c", ch);
		}

		public SelectStatementFragment parseSelect(QuerySpec spec) {
			QueryExp exp = spec.queryExp;
			SelectStatementFragment selectFrag = new SelectStatementFragment();
			
			//init tbl
			TableFragment tblFrag = new TableFragment();
			createAlias(tblFrag);
			tblFrag.name = exp.typeName;
			selectFrag.aliasMap.put(tblFrag.name, tblFrag);
			selectFrag.tblFrag = tblFrag;
			
			DStructType structType = getMainType(spec); 
			initFields(spec, structType, selectFrag);
			
			xgenerateQuery(spec, structType, selectFrag);
			
			if (selectFrag.fieldL.isEmpty()) {
				FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); //new FieldFragment();
				selectFrag.fieldL.add(fieldF);
			}
			
			return selectFrag;
		}
		
		private void xgenerateQuery(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			QueryExp exp = spec.queryExp;
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
//				String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "first");
//				if (fieldName == null) {
//					sc.o("SELECT TOP 1 * FROM %s", typeName);
//				} else {
//					sc.o("SELECT TOP 1 %s FROM %s", fieldName, typeName);
//				}
			} else if (selectFnHelper.isLastPresent(spec)) {
//				spec = doSelectLast(sc, spec, typeName);
			} else {
//				sc.o("SELECT * FROM %s", typeName);
			}
//			SqlStatement statement = new SqlStatement();
//
//			statement = pwheregen.generateAWhere(spec);
//			sc.o(statement.sql);
//			
//			generateQueryFns(sc, spec, typeName);
//			
//			sc.o(";");
//			statement.sql = sc.str;
//			return statement;
		}

		private void addFnField(String fnName, String fieldName, DStructType structType,
				SelectStatementFragment selectFrag) {
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
			FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, pair);
			fieldF.isStar = true;
			return fieldF;
		}
		
		private FieldFragment buildFieldFrag(DStructType structType, SelectStatementFragment selectFrag, TypePair pair) {
			return FragmentHelper.buildFieldFrag(structType, selectFrag, pair);
		}

		public String render(SelectStatementFragment selectFrag) {
			
			return selectFrag.render();
		}
		
		
	}


	@Test
	public void testPrimaryKey() {
		String src = buildSrc();
		FragmentParser parser = createFragmentParser(src); 
		
		QuerySpec spec= buildPrimaryKeyQuery("Flight", 1);
		SelectStatementFragment selectFrag = parser.parseSelect(spec);
		
		String sql = parser.render(selectFrag);
		log.log(sql);
		assertEquals("SELECT * FROM Flight as a WHERE a.field1 = ?", sql);
	}
	
	@Test
	public void testAllRows() {
		String src = buildSrc();
		FragmentParser parser = createFragmentParser(src); 
		
		QuerySpec spec= buildAllRowsQuery("Flight");
		SelectStatementFragment selectFrag = parser.parseSelect(spec);
		
		String sql = parser.render(selectFrag);
		assertEquals("SELECT * FROM Flight as a", sql);
	}
	
	@Test
	public void testOp() {
		String src = buildSrc();
		fragmentParser = createFragmentParser(src); 
		
		QuerySpec spec= buildOpQuery("Flight", "field2", 10);
		SelectStatementFragment selectFrag = fragmentParser.parseSelect(spec);
		
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
	
	
	private void runAndChk(SelectStatementFragment selectFrag, String expected) {
		String sql = fragmentParser.render(selectFrag);
		log.log(sql);
		assertEquals(expected, sql);
	}

	private SelectStatementFragment buildSelectFragment(String src) {
		LetStatementExp letStatementExp = buildFromSrc(src);
		QuerySpec spec= buildQuery((QueryExp) letStatementExp.value);
		SelectStatementFragment selectFrag = fragmentParser.parseSelect(spec);
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

	//---
	private Delia delia;
	private FactoryService factorySvc;
	private DTypeRegistry registry;
	private Runner runner;
	private QueryBuilderService queryBuilderSvc;
	private ScalarValueBuilder builder;

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
		
		FragmentParser parser = new FragmentParser(factorySvc, registry, runner);
		
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
		
		FragmentParser parser = new FragmentParser(factorySvc, registry, runner);
		
		this.queryBuilderSvc = factorySvc.getQueryBuilderService();
		this.builder = factorySvc.createScalarValueBuilder(registry);
		
		return parser;
	}

}
