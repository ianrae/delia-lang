package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSessionImpl;
import org.delia.api.MigrationAction;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.QueryExp;
import org.delia.dao.DeliaDao;
import org.delia.db.DBAccessContext;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.SqlHelperFactory;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.sql.fragment.DeleteFragmentParser;
import org.delia.db.sql.fragment.DeleteStatementFragment;
import org.delia.db.sql.fragment.FragmentParserService;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.TableInfo;
import org.delia.runner.RunnerImpl;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;


public class DeleteFragementParserTests extends FragmentParserTestBase {
	
	@Test
	public void testPrimaryKey() {
		String src = buildSrc();
		DeleteFragmentParser parser = createFragmentParser(src); 
		
		QuerySpec spec= buildPrimaryKeyQuery("Flight", 1);
		DeleteStatementFragment selectFrag = parser.parseDelete(spec, details);
		
		String sql = parser.renderDelete(selectFrag);
		log.log(sql);
		assertEquals("DELETE FROM Flight as a WHERE  a.field1 = ?", sql);
	}
	
	@Test
	public void testAllRows() {
		String src = buildSrc();
		DeleteFragmentParser parser = createFragmentParser(src); 
		
		QuerySpec spec= buildAllRowsQuery("Flight");
		DeleteStatementFragment selectFrag = parser.parseDelete(spec, details);
		
		String sql = parser.renderDelete(selectFrag);
		assertEquals("DELETE FROM Flight as a", sql);
	}
	
	@Test
	public void testOp() {
		String src = buildSrc();
		fragmentParser = createFragmentParser(src); 
		
		QuerySpec spec= buildOpQuery("Flight", "field2", 10);
		DeleteStatementFragment selectFrag = fragmentParser.parseDelete(spec, details);
		
		runAndChk(selectFrag, "DELETE FROM Flight as a WHERE  a.field2 = ?");
	}
	
	//TODO: add support for limit later
//	@Test
//	public void testLimit() {
//		String src = buildSrc();
//		src += " delete Flight[true].limit(4)";
//		DeleteStatementFragment selectFrag = buildSelectFragment(src); 
//		
//		runAndChk(selectFrag, "DELETE * FROM Flight as a ORDER BY a.field2 LIMIT 4 OFFSET 10");
//	}
	
	@Test
	public void test11Relation() {
		String src = buildSrcOneToOne();
		DeleteStatementFragment selectFrag = buildSelectFragment(src); 

		//[1] SQL:             SELECT a.id,b.id as addr FROM Customer as a LEFT JOIN Address as b ON b.cust=a.id  WHERE  a.id=?;  -- (55)
		runAndChk(selectFrag, "DELETE FROM Customer as a WHERE  a.id = ?");
		chkParamInt(selectFrag.statement, 1, 55);
	}

	//---

	private DeleteFragmentParser fragmentParser;
	private ScalarValueBuilder builder;
	private QueryDetails details = new QueryDetails();
	@Before
	public void init() {
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
		src += "\n  delete Customer[55]";
		return src;
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
		DBAccessContext dbctx = new DBAccessContext(runner);
		WhereFragmentGenerator whereGen = new WhereFragmentGenerator(factorySvc, registry, runner);
		FragmentParserService fpSvc = new FragmentParserService(factorySvc, registry, runner, tblinfoL, dao.getDbInterface(), dbctx, sqlHelperFactory, whereGen);
		DeleteFragmentParser parser = new DeleteFragmentParser(factorySvc, fpSvc, tblinfoL, dao.getDbInterface(), dbctx, sqlHelperFactory, whereGen);
		whereGen.tableFragmentMaker = parser;
		return parser;
	}

	private void runAndChk(DeleteStatementFragment selectFrag, String expected) {
		String sql = fragmentParser.renderDelete(selectFrag);
		log.log(sql);
		assertEquals(expected, sql);
	}

	private DeleteStatementFragment buildSelectFragment(String src) {
		DeleteStatementExp deleteStatementExp = buildFromSrc(src);
		QuerySpec spec= buildQuery((QueryExp) deleteStatementExp.queryExp);
		DeleteStatementFragment selectFrag = fragmentParser.parseDelete(spec, details);
		return selectFrag;
	}

	private DeleteStatementExp buildFromSrc(String src) {
		DeliaDao dao = createDao(); 
		Delia xdelia = dao.getDelia();
		xdelia.getOptions().migrationAction = MigrationAction.GENERATE_MIGRATION_PLAN;
		dao.getDbInterface().getCapabilities().setRequiresSchemaMigration(true);
		this.fragmentParser = createFragmentParser(dao, src); 
		
//		List<Exp> expL = dao.getMostRecentSess().
		DeliaSessionImpl sessImpl = (DeliaSessionImpl) dao.getMostRecentSess();
		DeleteStatementExp deletetatementExp = null;
		for(Exp exp: sessImpl.expL) {
			if (exp instanceof DeleteStatementExp) {
				deletetatementExp = (DeleteStatementExp) exp;
			}
		}
		return deletetatementExp;
	}

	private void chkParamInt(SqlStatement statement, int n, int expected) {
		assertEquals(n, statement.paramL.size());
		assertEquals(expected, statement.paramL.get(n-1).asInt());
	}

}
