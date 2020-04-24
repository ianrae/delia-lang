package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.delia.api.Delia;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.QueryBuilderService;
import org.delia.db.QuerySpec;
import org.delia.db.ValueHelper;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.StrCreator;
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

		@Override
		public String render() {
			if (isStar) {
				return "*";
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
			
			initFields(spec, selectFrag);
			
			return selectFrag;
		}

		private void initFields(QuerySpec spec, SelectStatementFragment selectFrag) {
			DStructType structType = (DStructType) registry.findTypeOrSchemaVersionType(spec.queryExp.typeName);
			
			QueryType queryType = queryDetectorSvc.detectQueryType(spec);
			switch(queryType) {
			case ALL_ROWS:
			{
				FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); //new FieldFragment();
				selectFrag.fieldL.add(fieldF);
//				addWhereExist(sc, spec);
			}
				break;
			case OP:
//				addWhereClauseOp(sc, spec, typeName, tbl, statement);
				break;
			case PRIMARY_KEY:
			default:
			{
				FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); //new FieldFragment();
				selectFrag.fieldL.add(fieldF);
//				addWhereClausePrimaryKey(sc, spec, spec.queryExp.filter, typeName, tbl, statement);
				whereGen.addWhereClausePrimaryKey(spec, spec.queryExp.filter, structType, selectFrag);
			}
				break;
			}
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
	
	//---
	private Delia delia;
	private FactoryService factorySvc;
	private DTypeRegistry registry;
	private Runner runner;
	private QueryBuilderService queryBuilderSvc;
	private ScalarValueBuilder builder;

	
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


}
