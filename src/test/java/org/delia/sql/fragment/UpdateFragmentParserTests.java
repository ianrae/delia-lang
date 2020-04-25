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
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.fragment.FieldFragment;
import org.delia.db.sql.fragment.FragmentHelper;
import org.delia.db.sql.fragment.OrderByFragment;
import org.delia.db.sql.fragment.SelectFragmentParser;
import org.delia.db.sql.fragment.SelectStatementFragment;
import org.delia.db.sql.fragment.TableFragment;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.table.ListWalker;
import org.delia.db.sql.table.TableInfo;
import org.delia.error.SimpleErrorTracker;
import org.delia.runner.ConversionResult;
import org.delia.runner.DsonToDValueConverter;
import org.delia.runner.Runner;
import org.delia.runner.RunnerImpl;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigServiceImpl;
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

		//parallel arrays
		public List<String> setValuesL = new ArrayList<>();
		
		@Override
		public String render() {
			StrCreator sc = new StrCreator();
			sc.o("UPDATE");
			//renderEarly(sc);
			sc.o(" %s", tblFrag.render());
			renderUpdateFields(sc);
//			renderIfPresent(sc, joinFrag);

			if (! whereL.isEmpty()) {
				sc.o(" WHERE");
				renderWhereL(sc);
			}

//			renderIfPresent(sc, orderByFrag);
			renderIfPresent(sc, limitFrag);
			return sc.str;
		}
		
		protected void renderUpdateFields(StrCreator sc) {
			if (fieldL.isEmpty()) {
				return;
			}
			
			sc.o(" SET ");
			int index = 0;
			ListWalker<FieldFragment> walker = new ListWalker<>(fieldL);
			while(walker.hasNext()) {
				FieldFragment ff = walker.next();
				String value = setValuesL.get(index);
				sc.o("%s = %s", renderSetField(ff), value);
				walker.addIfNotLast(sc, ",");
				index++;
			}
		}
		
		private String renderSetField(FieldFragment fieldF) {
			String suffix = fieldF.asName == null ? "" : " as " + fieldF.asName;
			return String.format("%s%s", fieldF.renderAsAliasedFrag(), suffix);
		}
		
	}	

	//single use!!!
	public static class UpdateFragmentParser extends SelectFragmentParser {

		public UpdateFragmentParser(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator, List<TableInfo> tblinfoL, DBInterface dbInterface, 
				SqlHelperFactory sqlHelperFactory, WhereFragmentGenerator whereGen) {
			super(factorySvc, registry, varEvaluator, tblinfoL, dbInterface, sqlHelperFactory, whereGen);
		}

		public UpdateStatementFragment parseUpdate(QuerySpec spec, QueryDetails details, DValue partialVal) {
			UpdateStatementFragment selectFrag = new UpdateStatementFragment();

			//init tbl
			DStructType structType = getMainType(spec); 
			TableFragment tblFrag = createTable(structType, selectFrag);
			selectFrag.tblFrag = tblFrag;

			generateSetFields(spec, structType, selectFrag, partialVal);
			initFieldsAndWhere(spec, structType, selectFrag);
			
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

			return selectFrag;
		}

		private void generateSetFields(QuerySpec spec, DStructType structType, UpdateStatementFragment selectFrag,
				DValue partialVal) {
			//we assume partialVal same type as structType!! (or maybe a base class)
			
			int index = selectFrag.fieldL.size(); //setValuesL is parallel array to fieldL
			if (index != 0) {
				
				log.log("WHY FILLING INNNNNNNNN");
				for(int i = 0; i < index; i++) {
					selectFrag.setValuesL.add("????");
				}
				DeliaExceptionHelper.throwError("unexpeced-fields-in-update", "should not occur");
			}
			
			for(String fieldName: partialVal.asMap().keySet()) {
				DValue inner = partialVal.asMap().get(fieldName);

				TypePair pair = DValueHelper.findField(structType, fieldName);
				FieldFragment ff = FragmentHelper.buildFieldFrag(structType, selectFrag, pair);
				String valstr = inner.asString();
				selectFrag.setValuesL.add(valstr == null ? "null" : valstr);
				selectFrag.fieldL.add(ff);
				
				index++;
			}
		}

		protected boolean needJoin(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag, QueryDetails details) {
			if (needJoinBase(spec, structType, selectFrag, details)) {
				return true;
			}

//			if (selectFrag.joinFrag == null) {
//				return false;
//			}
//
//			String alias = savedJoinedFrag.joinTblFrag.alias;
//
//			boolean mentioned = false;
//			if (selectFrag.orderByFrag != null) {
//				if (alias.equals(selectFrag.orderByFrag.alias)) {
//					mentioned = true;
//				}
//				for(OrderByFragment obff: selectFrag.orderByFrag.additionalL) {
//					if (alias.equals(obff.alias)) {
//						mentioned = true;
//						break;
//					}
//				}
//			}
//
//
//			if (mentioned) {
//				log.log("need join..");
//				return true;
//			}
			return false;
		}


		public void generateUpdateFns(QuerySpec spec, DStructType structType, UpdateStatementFragment selectFrag) {
			//orderby supported only by MySQL which delia does not support
//			this.doOrderByIfPresent(spec, structType, selectFrag);
			this.doLimitIfPresent(spec, structType, selectFrag);
//			this.doOffsetIfPresent(spec, structType, selectFrag);
		}


//		protected void doOffsetIfPresent(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
//			QueryFuncExp qfexp = selectFnHelper.findFn(spec, "offset");
//			if (qfexp == null) {
//				return;
//			}
//			IntegerExp exp = (IntegerExp) qfexp.argL.get(0);
//			Integer n = exp.val;
//
//			OffsetFragment frag = new OffsetFragment(n);
//			selectFrag.offsetFrag = frag;
//		}

		public String renderUpdate(UpdateStatementFragment selectFrag) {
			selectFrag.statement.sql = selectFrag.render();
			return selectFrag.statement.sql;
		}
	}	

	@Test
	public void testPrimaryKey() {
		String src = buildSrc();
		src += " update Flight[1] {field2: 111}";
		
		UpdateStatementExp updateStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(updateStatementExp);
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = 111 WHERE a.field1 = ?");
	}

	@Test
	public void testAllRows() {
		String src = buildSrc();
		src += " update Flight[true] {field2: 111}";
		
		UpdateStatementExp updateStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(updateStatementExp);
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = 111");
	}

	@Test
	public void testOp() {
		String src = buildSrc();
		src += " update Flight[field1 > 0] {field2: 111}";
		
		UpdateStatementExp updateStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(updateStatementExp);
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = 111 WHERE a.field1 > ?");
	}
	@Test
	public void testOpNoPrimaryKey() {
		String src = buildSrcNoPrimaryKey();
		src += " update Flight[field1 > 0] {field2: 111}";
		
		UpdateStatementExp updateStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(updateStatementExp);
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = 111 WHERE a.field1 > ?");
	}

	@Test
	public void testBasic() {
		String src = buildSrc();
		src += " update Flight[1] {field2: 111}";
		
		UpdateStatementExp updateStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(updateStatementExp);
		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
		
		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = 111 WHERE a.field1 = ?");
	}
	private DValue convertToDVal(UpdateStatementExp updateStatementExp) {
		DStructType structType = (DStructType) registry.getType("Flight");
		ConversionResult cres = buildPartialValue(structType, updateStatementExp.dsonExp);
		assertEquals(0, cres.localET.errorCount());
		return cres.dval;
	}
	
//TODO: support orderBy
//	@Test
//	public void testOrderBy() {
//		String src = buildSrc();
//		src += " let x = Flight[true].orderBy('field2')";
//		UpdateStatementFragment selectFrag = buildUpdateFragment(src); 
//
//		runAndChk(selectFrag, "SELECT * FROM Flight as a ORDER BY a.field2");
//	}
	
	//TODO: support limit
//	@Test
//	public void testOrderByLimit() {
//		String src = buildSrc();
//		src += " update Flight[true].limit(4) {field2: 111}";
//		
//		UpdateStatementExp updateStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(updateStatementExp);
//		UpdateStatementFragment selectFrag = buildUpdateFragment(updateStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = 111");
//	}


	//TODO: support relations
//	@Test
//	public void test11Relation() {
//		String src = buildSrcOneToOne();
//		UpdateStatementFragment selectFrag = buildUpdateFragment(src); 
//
//		//[1] SQL:             SELECT a.id,b.id as addr FROM Customer as a LEFT JOIN Address as b ON b.cust=a.id  WHERE  a.id=?;  -- (55)
//		runAndChk(selectFrag, "SELECT * FROM Customer as a WHERE  a.id = ?");
//		chkParamInt(selectFrag.statement, 1, 55);
//	}

	//---
	private Delia delia;
	private FactoryService factorySvc;
	private DTypeRegistry registry;
	private Runner runner;
	private QueryBuilderService queryBuilderSvc;
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
//	private String buildSrcOneToOne() {
//		String src = " type Customer struct {id int unique, wid int, relation addr Address optional one parent } end";
//		src += "\n type Address struct {id int unique, relation cust Customer optional one } end";
//		src += "\n  insert Customer {id: 55, wid: 33}";
//		src += "\n  insert Address {id: 100, cust: 55 }";
//		src += "\n  update Customer[55] {wid: 333}";
//		return src;
//	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}

	private QuerySpec buildQuery(QueryExp exp) {
		QuerySpec spec= queryBuilderSvc.buildSpec(exp, runner);
		return spec;
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

	private UpdateStatementFragment buildUpdateFragment(UpdateStatementExp exp, DValue dval) {
		QuerySpec spec= buildQuery((QueryExp) exp.queryExp);
		UpdateStatementFragment selectFrag = fragmentParser.parseUpdate(spec, details, dval);
		return selectFrag;
	}

	private UpdateStatementExp buildFromSrc(String src) {
		DeliaDao dao = createDao(); 
		Delia xdelia = dao.getDelia();
		xdelia.getOptions().migrationAction = MigrationAction.GENERATE_MIGRATION_PLAN;
		dao.getDbInterface().getCapabilities().setRequiresSchemaMigration(true);
		this.fragmentParser = createFragmentParser(dao, src); 

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

}
