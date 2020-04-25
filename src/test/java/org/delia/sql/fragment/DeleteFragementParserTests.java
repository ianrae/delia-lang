package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.SqlHelperFactory;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.fragment.FieldFragment;
import org.delia.db.sql.fragment.FragmentParser;
import org.delia.db.sql.fragment.JoinFragment;
import org.delia.db.sql.fragment.LimitFragment;
import org.delia.db.sql.fragment.OffsetFragment;
import org.delia.db.sql.fragment.OrderByFragment;
import org.delia.db.sql.fragment.SelectStatementFragment;
import org.delia.db.sql.fragment.SqlFragment;
import org.delia.db.sql.fragment.StatementFragmentBase;
import org.delia.db.sql.fragment.TableFragment;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.ListWalker;
import org.delia.db.sql.table.TableInfo;
import org.delia.runner.Runner;
import org.delia.runner.RunnerImpl;
import org.delia.runner.VarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;


public class DeleteFragementParserTests extends NewBDDBase {
	
	public static class DeleteStatementFragment extends StatementFragmentBase {
		
		public List<SqlFragment> earlyL = new ArrayList<>();
		public List<FieldFragment> fieldL = new ArrayList<>();
		public TableFragment tblFrag;
		public JoinFragment joinFrag; //TODO later a list
		public List<SqlFragment> whereL = new ArrayList<>();
		public LimitFragment limitFrag = null;
		
		@Override
		public String render() {
			StrCreator sc = new StrCreator();
			sc.o("DELETE ");
			renderEarly(sc);
			renderFields(sc);
			sc.o(" FROM %s", tblFrag.render());
			renderIfPresent(sc, joinFrag);
			
			if (! whereL.isEmpty()) {
				sc.o(" WHERE ");
				renderWhereL(sc);
			}
			
			renderIfPresent(sc, limitFrag);
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

		public void clearFieldList() {
			fieldL.clear();
		}

	}	
	
	public static class DeleteFragmentParser extends FragmentParser {

		public DeleteFragmentParser(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator,
				List<TableInfo> tblinfoL, DBInterface dbInterface, SqlHelperFactory sqlHelperFactory,
				WhereFragmentGenerator whereGen) {
			super(factorySvc, registry, varEvaluator, tblinfoL, dbInterface, sqlHelperFactory, whereGen);
		}
		
		public DeleteStatementFragment parseDelete(QuerySpec spec, QueryDetails details) {
			DeleteStatementFragment selectFrag = new DeleteStatementFragment();
			
			//init tbl
			DStructType structType = getMainType(spec); 
			TableFragment tblFrag = createTable(structType, selectFrag);
			selectFrag.tblFrag = tblFrag;
			
			initFields(spec, structType, selectFrag);
//			addJoins(spec, structType, selectFrag, details);

			generateDeleteFns(spec, structType, selectFrag);
			
			fixupForParentFields(structType, selectFrag);
			if (needJoinBase(spec, structType, selectFrag, details)) {
				//used saved join if we have one
				if (savedJoinedFrag == null) {
					addJoins(spec, structType, selectFrag, details);
				} else {
					selectFrag.joinFrag = savedJoinedFrag;
				}
			}

			if (selectFrag.fieldL.isEmpty()) {
				FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); //new FieldFragment();
				selectFrag.fieldL.add(fieldF);
			}
			
			
			return selectFrag;
		}
		
		public void generateDeleteFns(QuerySpec spec, DStructType structType, DeleteStatementFragment selectFrag) {
			this.doLimitIfPresent(spec, structType, selectFrag);
		}
		
		public String renderDelete(DeleteStatementFragment selectFrag) {
			selectFrag.statement.sql = selectFrag.render();
			return selectFrag.statement.sql;
		}
		
	}
	
	@Test
	public void testPrimaryKey() {
		String src = buildSrc();
		DeleteFragmentParser parser = createFragmentParser(src); 
		
		QuerySpec spec= buildPrimaryKeyQuery("Flight", 1);
		DeleteStatementFragment selectFrag = parser.parseDelete(spec, details);
		
		String sql = parser.renderDelete(selectFrag);
		log.log(sql);
		assertEquals("SELECT * FROM Flight as a WHERE  a.field1 = ?", sql);
	}
	
	@Test
	public void testAllRows() {
		String src = buildSrc();
		DeleteFragmentParser parser = createFragmentParser(src); 
		
		QuerySpec spec= buildAllRowsQuery("Flight");
		DeleteStatementFragment selectFrag = parser.parseDelete(spec, details);
		
		String sql = parser.renderDelete(selectFrag);
		assertEquals("SELECT * FROM Flight as a", sql);
	}
	
	@Test
	public void testOp() {
		String src = buildSrc();
		fragmentParser = createFragmentParser(src); 
		
		QuerySpec spec= buildOpQuery("Flight", "field2", 10);
		DeleteStatementFragment selectFrag = fragmentParser.parseDelete(spec, details);
		
		runAndChk(selectFrag, "SELECT * FROM Flight as a WHERE  a.field2 = ?");
	}
	
	@Test
	public void testMax() {
		String src = buildSrc();
		src += " let x = Flight[1].field1.max()";
		DeleteStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT MAX(a.field1) FROM Flight as a WHERE  a.field1 = ?");
	}
	
	@Test
	public void testMin() {
		String src = buildSrc();
		src += " let x = Flight[1].field1.min()";
		DeleteStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT MIN(a.field1) FROM Flight as a WHERE  a.field1 = ?");
	}
	@Test
	public void testCount() {
		String src = buildSrc();
		src += " let x = Flight[true].count()";
		DeleteStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT COUNT(*) FROM Flight as a");
	}
	@Test
	public void testCountField() {
		String src = buildSrc();
		src += " let x = Flight[true].field1.count()";
		DeleteStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT COUNT(a.field1) FROM Flight as a");
	}
	
	@Test
	public void testFirst() {
		String src = buildSrc();
		src += " let x = Flight[true].first()";
		DeleteStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT TOP 1 * FROM Flight as a");
	}
	@Test
	public void testFirstField() {
		String src = buildSrc();
		src += " let x = Flight[true].field1.first()";
		DeleteStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT TOP 1 a.field1 FROM Flight as a");
	}
	
	@Test
	public void testLast() {
		String src = buildSrc();
		src += " let x = Flight[true].last()";
		DeleteStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT TOP 1 * FROM Flight as a ORDER BY a.field1 desc");
	}
	@Test
	public void testLastField() {
		String src = buildSrc();
		src += " let x = Flight[true].field1.last()";
		DeleteStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT TOP 1 a.field1 FROM Flight as a ORDER BY a.field1 desc");
	}
	@Test
	public void testLastFieldOrderBy() {
		String src = buildSrc();
		src += " let x = Flight[true].orderBy('field2').field1.last()";
		DeleteStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT TOP 1 a.field1 FROM Flight as a ORDER BY a.field2, a.field1 desc");
	}
	
	
	@Test
	public void testLastNoPrimaryKey() {
		String src = buildSrcNoPrimaryKey();
		src += " let x = Flight[true].last()";
		DeleteStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT TOP 1 * FROM Flight as a");
	}
	@Test
	public void testLastNoPrimaryKeyField() {
		String src = buildSrcNoPrimaryKey();
		src += " let x = Flight[true].field2.last()";
		DeleteStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT TOP 1 a.field2 FROM Flight as a ORDER BY a.field2 desc");
	}
	
	@Test
	public void testOrderBy() {
		String src = buildSrc();
		src += " let x = Flight[true].orderBy('field2')";
		DeleteStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT * FROM Flight as a ORDER BY a.field2");
	}
	@Test
	public void testOrderByLimitOffset() {
		String src = buildSrc();
		src += " let x = Flight[true].orderBy('field2').limit(4).offset(10)";
		DeleteStatementFragment selectFrag = buildSelectFragment(src); 
		
		runAndChk(selectFrag, "SELECT * FROM Flight as a ORDER BY a.field2 LIMIT 4 OFFSET 10");
	}
	
	@Test
	public void test11Relation() {
		String src = buildSrcOneToOne();
		DeleteStatementFragment selectFrag = buildSelectFragment(src); 

		//[1] SQL:             SELECT a.id,b.id as addr FROM Customer as a LEFT JOIN Address as b ON b.cust=a.id  WHERE  a.id=?;  -- (55)
		runAndChk(selectFrag, "SELECT * FROM Customer as a WHERE  a.id = ?");
		chkParamInt(selectFrag.statement, 1, 55);
	}

	//---
	private Delia delia;
	private FactoryService factorySvc;
	private DTypeRegistry registry;
	private Runner runner;
	private QueryBuilderService queryBuilderSvc;
	private ScalarValueBuilder builder;
	private QueryDetails details = new QueryDetails();

	private DeleteFragmentParser fragmentParser;
	

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

	private DeleteFragmentParser createFragmentParser(String src) {
		DeliaDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);
		
		this.delia = dao.getDelia();
		this.factorySvc = delia.getFactoryService();
		this.registry = dao.getRegistry();
		this.runner = new RunnerImpl(factorySvc, dao.getDbInterface());
		
		DeleteFragmentParser parser = createParser(dao); 
		
		this.queryBuilderSvc = factorySvc.getQueryBuilderService();
		this.builder = factorySvc.createScalarValueBuilder(registry);
		
		return parser;
	}

	private DeleteFragmentParser createFragmentParser(DeliaDao dao, String src) {
		boolean b = dao.initialize(src);
		assertEquals(true, b);
		
		this.delia = dao.getDelia();
		this.factorySvc = delia.getFactoryService();
		this.registry = dao.getRegistry();
		this.runner = new RunnerImpl(factorySvc, dao.getDbInterface());
		
		DeleteFragmentParser parser = createParser(dao); 
		
		this.queryBuilderSvc = factorySvc.getQueryBuilderService();
		this.builder = factorySvc.createScalarValueBuilder(registry);
		
		return parser;
	}
	private DeleteFragmentParser createParser(DeliaDao dao) {
		SqlHelperFactory sqlHelperFactory = new H2SqlHelperFactory(factorySvc);
		List<TableInfo> tblinfoL = new ArrayList<>();		
		WhereFragmentGenerator whereGen = new WhereFragmentGenerator(factorySvc, registry, runner);
		DeleteFragmentParser parser = new DeleteFragmentParser(factorySvc, registry, runner, tblinfoL, dao.getDbInterface(), sqlHelperFactory, whereGen);
		whereGen.tableFragmentMaker = parser;
		return parser;
	}

	private void runAndChk(DeleteStatementFragment selectFrag, String expected) {
		String sql = fragmentParser.renderDelete(selectFrag);
		log.log(sql);
		assertEquals(expected, sql);
	}

	private DeleteStatementFragment buildSelectFragment(String src) {
		LetStatementExp letStatementExp = buildFromSrc(src);
		QuerySpec spec= buildQuery((QueryExp) letStatementExp.value);
		DeleteStatementFragment selectFrag = fragmentParser.parseDelete(spec, details);
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

	private void chkParamInt(SqlStatement statement, int n, int expected) {
		assertEquals(n, statement.paramL.size());
		assertEquals(expected, statement.paramL.get(n-1).asInt());
	}

}
