package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
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
import org.delia.db.sql.fragment.FragmentHelper;
import org.delia.db.sql.fragment.OffsetFragment;
import org.delia.db.sql.fragment.OrderByFragment;
import org.delia.db.sql.fragment.SelectFragmentParser;
import org.delia.db.sql.fragment.SelectStatementFragment;
import org.delia.db.sql.fragment.TableFragment;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.prepared.SqlStatement;
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


public class UpdateFragmentParserTests extends NewBDDBase {

	public static class UpdateStatementFragment extends SelectStatementFragment {

		@Override
		public String render() {
			StrCreator sc = new StrCreator();
			sc.o("UPPPP ");
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
	}	

	//single use!!!
	public static class UpdateFragmentParser extends SelectFragmentParser {

		public UpdateFragmentParser(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator, List<TableInfo> tblinfoL, DBInterface dbInterface, 
				SqlHelperFactory sqlHelperFactory, WhereFragmentGenerator whereGen) {
			super(factorySvc, registry, varEvaluator, tblinfoL, dbInterface, sqlHelperFactory, whereGen);
		}

		public UpdateStatementFragment parseUpdate(QuerySpec spec, QueryDetails details) {
			UpdateStatementFragment selectFrag = new UpdateStatementFragment();

			//init tbl
			DStructType structType = getMainType(spec); 
			TableFragment tblFrag = createTable(structType, selectFrag);
			selectFrag.tblFrag = tblFrag;

			initFields(spec, structType, selectFrag);
			//no min,max,etc in UPDATE

			generateUpdateFns(spec, structType, selectFrag);

			fixupForParentFields(structType, selectFrag);
			if (needJoin(spec, structType, selectFrag, details)) {
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

		protected boolean needJoin(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag, QueryDetails details) {
			if (needJoinBase(spec, structType, selectFrag, details)) {
				return true;
			}

			if (selectFrag.joinFrag == null) {
				return false;
			}

			String alias = savedJoinedFrag.joinTblFrag.alias;

			boolean mentioned = false;
			if (selectFrag.orderByFrag != null) {
				if (alias.equals(selectFrag.orderByFrag.alias)) {
					mentioned = true;
				}
				for(OrderByFragment obff: selectFrag.orderByFrag.additionalL) {
					if (alias.equals(obff.alias)) {
						mentioned = true;
						break;
					}
				}
			}


			if (mentioned) {
				log.log("need join..");
				return true;
			}
			return false;
		}


		public void generateUpdateFns(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			//orderby supported only by MySQL which delia does not support
//			this.doOrderByIfPresent(spec, structType, selectFrag);
			this.doLimitIfPresent(spec, structType, selectFrag);
			this.doOffsetIfPresent(spec, structType, selectFrag);
		}

		protected void doOrderByIfPresent(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
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
			addToOrderBy(frag, selectFrag);
		}
		protected void addToOrderBy(OrderByFragment frag, SelectStatementFragment selectFrag) {
			OrderByFragment orderByFrag = selectFrag.orderByFrag;
			if (orderByFrag == null) {
				selectFrag.orderByFrag = frag;
			} else {
				//only add if different
				if (areEqualOrderBy(selectFrag.orderByFrag, frag)) {
					return;
				}
				for(OrderByFragment obf: selectFrag.orderByFrag.additionalL) {
					if (areEqualOrderBy(obf, frag)) {
						return;
					}
				}

				OrderByFragment tmp = selectFrag.orderByFrag; //swap
				selectFrag.orderByFrag = frag;
				selectFrag.orderByFrag.additionalL.add(tmp);
			}
		}
		protected boolean areEqualOrderBy(OrderByFragment orderByFrag, OrderByFragment frag) {
			if((frag.alias != null &&frag.alias.equals(orderByFrag.alias)) && frag.name.equals(orderByFrag.name)) {
				if (frag.asc != null && frag.asc.equals(orderByFrag.asc)) {
					return true;
				} else if (frag.asc == null && orderByFrag.asc == null) {
					return true;
				}
			}
			return false;
		}

		protected void doOffsetIfPresent(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			QueryFuncExp qfexp = selectFnHelper.findFn(spec, "offset");
			if (qfexp == null) {
				return;
			}
			IntegerExp exp = (IntegerExp) qfexp.argL.get(0);
			Integer n = exp.val;

			OffsetFragment frag = new OffsetFragment(n);
			selectFrag.offsetFrag = frag;
		}

		protected void forceAddOrderByPrimaryKey(DStructType structType, SelectStatementFragment selectFrag, String asc) {
			TypePair pair = DValueHelper.findPrimaryKeyFieldPair(structType);
			if (pair == null) {
				return; //no primary key
			}
			forceAddOrderByField(structType, pair.name, asc, selectFrag);
		}
		protected void forceAddOrderByField(DStructType structType, String fieldName, String asc, SelectStatementFragment selectFrag) {
			OrderByFragment orderByFrag = FragmentHelper.buildOrderByFrag(structType, fieldName, asc, selectFrag);
			selectFrag.orderByFrag = orderByFrag;
		}

		protected void addFnField(String fnName, String fieldName, DStructType structType, SelectStatementFragment selectFrag) {
			FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, fieldName);
			if (fieldF == null) {
				DeliaExceptionHelper.throwError("unknown-field", "Field %s.%s unknown in %s function", structType.getName(), fieldName, fnName);
			}
			fieldF.fnName = fnName;
			addOrReplace(selectFrag, fieldF);
		}

		public String renderUpdate(UpdateStatementFragment selectFrag) {
			selectFrag.statement.sql = selectFrag.render();
			return selectFrag.statement.sql;
		}
	}	

	@Test
	public void testPrimaryKey() {
		String src = buildSrc();
		UpdateFragmentParser parser = createFragmentParser(src); 

		QuerySpec spec= buildPrimaryKeyQuery("Flight", 1);
		UpdateStatementFragment selectFrag = parser.parseUpdate(spec, details);

		String sql = parser.renderSelect(selectFrag);
		log.log(sql);
		assertEquals("SELECT * FROM Flight as a WHERE  a.field1 = ?", sql);
	}

	@Test
	public void testAllRows() {
		String src = buildSrc();
		UpdateFragmentParser parser = createFragmentParser(src); 

		QuerySpec spec= buildAllRowsQuery("Flight");
		UpdateStatementFragment selectFrag = parser.parseUpdate(spec, details);

		String sql = parser.renderSelect(selectFrag);
		assertEquals("SELECT * FROM Flight as a", sql);
	}

	@Test
	public void testOp() {
		String src = buildSrc();
		fragmentParser = createFragmentParser(src); 

		QuerySpec spec= buildOpQuery("Flight", "field2", 10);
		UpdateStatementFragment selectFrag = fragmentParser.parseUpdate(spec, details);

		runAndChk(selectFrag, "SELECT * FROM Flight as a WHERE  a.field2 = ?");
	}

	@Test
	public void testMax() {
		String src = buildSrc();
		src += " let x = Flight[1].field1.max()";
		UpdateStatementFragment selectFrag = buildSelectFragment(src); 

		runAndChk(selectFrag, "SELECT MAX(a.field1) FROM Flight as a WHERE  a.field1 = ?");
	}

	@Test
	public void testMin() {
		String src = buildSrc();
		src += " let x = Flight[1].field1.min()";
		UpdateStatementFragment selectFrag = buildSelectFragment(src); 

		runAndChk(selectFrag, "SELECT MIN(a.field1) FROM Flight as a WHERE  a.field1 = ?");
	}
	@Test
	public void testCount() {
		String src = buildSrc();
		src += " let x = Flight[true].count()";
		UpdateStatementFragment selectFrag = buildSelectFragment(src); 

		runAndChk(selectFrag, "SELECT COUNT(*) FROM Flight as a");
	}
	@Test
	public void testCountField() {
		String src = buildSrc();
		src += " let x = Flight[true].field1.count()";
		UpdateStatementFragment selectFrag = buildSelectFragment(src); 

		runAndChk(selectFrag, "SELECT COUNT(a.field1) FROM Flight as a");
	}

	@Test
	public void testFirst() {
		String src = buildSrc();
		src += " let x = Flight[true].first()";
		UpdateStatementFragment selectFrag = buildSelectFragment(src); 

		runAndChk(selectFrag, "SELECT TOP 1 * FROM Flight as a");
	}
	@Test
	public void testFirstField() {
		String src = buildSrc();
		src += " let x = Flight[true].field1.first()";
		UpdateStatementFragment selectFrag = buildSelectFragment(src); 

		runAndChk(selectFrag, "SELECT TOP 1 a.field1 FROM Flight as a");
	}

	@Test
	public void testLast() {
		String src = buildSrc();
		src += " let x = Flight[true].last()";
		UpdateStatementFragment selectFrag = buildSelectFragment(src); 

		runAndChk(selectFrag, "SELECT TOP 1 * FROM Flight as a ORDER BY a.field1 desc");
	}
	@Test
	public void testLastField() {
		String src = buildSrc();
		src += " let x = Flight[true].field1.last()";
		UpdateStatementFragment selectFrag = buildSelectFragment(src); 

		runAndChk(selectFrag, "SELECT TOP 1 a.field1 FROM Flight as a ORDER BY a.field1 desc");
	}
	@Test
	public void testLastFieldOrderBy() {
		String src = buildSrc();
		src += " let x = Flight[true].orderBy('field2').field1.last()";
		UpdateStatementFragment selectFrag = buildSelectFragment(src); 

		runAndChk(selectFrag, "SELECT TOP 1 a.field1 FROM Flight as a ORDER BY a.field2, a.field1 desc");
	}


	@Test
	public void testLastNoPrimaryKey() {
		String src = buildSrcNoPrimaryKey();
		src += " let x = Flight[true].last()";
		UpdateStatementFragment selectFrag = buildSelectFragment(src); 

		runAndChk(selectFrag, "SELECT TOP 1 * FROM Flight as a");
	}
	@Test
	public void testLastNoPrimaryKeyField() {
		String src = buildSrcNoPrimaryKey();
		src += " let x = Flight[true].field2.last()";
		UpdateStatementFragment selectFrag = buildSelectFragment(src); 

		runAndChk(selectFrag, "SELECT TOP 1 a.field2 FROM Flight as a ORDER BY a.field2 desc");
	}

	@Test
	public void testOrderBy() {
		String src = buildSrc();
		src += " let x = Flight[true].orderBy('field2')";
		UpdateStatementFragment selectFrag = buildSelectFragment(src); 

		runAndChk(selectFrag, "SELECT * FROM Flight as a ORDER BY a.field2");
	}
	@Test
	public void testOrderByLimitOffset() {
		String src = buildSrc();
		src += " let x = Flight[true].orderBy('field2').limit(4).offset(10)";
		UpdateStatementFragment selectFrag = buildSelectFragment(src); 

		runAndChk(selectFrag, "SELECT * FROM Flight as a ORDER BY a.field2 LIMIT 4 OFFSET 10");
	}

	@Test
	public void test11Relation() {
		String src = buildSrcOneToOne();
		UpdateStatementFragment selectFrag = buildSelectFragment(src); 

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

	private UpdateFragmentParser fragmentParser;


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

	private UpdateFragmentParser createFragmentParser(String src) {
		DeliaDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		this.delia = dao.getDelia();
		this.factorySvc = delia.getFactoryService();
		this.registry = dao.getRegistry();
		this.runner = new RunnerImpl(factorySvc, dao.getDbInterface());

		UpdateFragmentParser parser = createParser(dao); 

		this.queryBuilderSvc = factorySvc.getQueryBuilderService();
		this.builder = factorySvc.createScalarValueBuilder(registry);

		return parser;
	}

	private UpdateFragmentParser createFragmentParser(DeliaDao dao, String src) {
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		this.delia = dao.getDelia();
		this.factorySvc = delia.getFactoryService();
		this.registry = dao.getRegistry();
		this.runner = new RunnerImpl(factorySvc, dao.getDbInterface());

		UpdateFragmentParser parser = createParser(dao); 

		this.queryBuilderSvc = factorySvc.getQueryBuilderService();
		this.builder = factorySvc.createScalarValueBuilder(registry);

		return parser;
	}
	private UpdateFragmentParser createParser(DeliaDao dao) {
		SqlHelperFactory sqlHelperFactory = new H2SqlHelperFactory(factorySvc);
		List<TableInfo> tblinfoL = new ArrayList<>();		
		WhereFragmentGenerator whereGen = new WhereFragmentGenerator(factorySvc, registry, runner);
		UpdateFragmentParser parser = new UpdateFragmentParser(factorySvc, registry, runner, tblinfoL, dao.getDbInterface(), sqlHelperFactory, whereGen);
		whereGen.tableFragmentMaker = parser;
		return parser;
	}

	private void runAndChk(UpdateStatementFragment selectFrag, String expected) {
		String sql = fragmentParser.renderSelect(selectFrag);
		log.log(sql);
		assertEquals(expected, sql);
	}

	private UpdateStatementFragment buildSelectFragment(String src) {
		LetStatementExp letStatementExp = buildFromSrc(src);
		QuerySpec spec= buildQuery((QueryExp) letStatementExp.value);
		UpdateStatementFragment selectFrag = fragmentParser.parseUpdate(spec, details);
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
