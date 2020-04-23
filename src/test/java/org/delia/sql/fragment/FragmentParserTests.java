package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

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
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;
import org.delia.runner.Runner;
import org.delia.runner.RunnerImpl;
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
			return super.render();
		}
	}
	
	public static class SelectStatementFragment implements SqlFragment {
		public List<FieldFragment> fieldL = new ArrayList<>();
		public TableFragment tblFrag;
		
		
		@Override
		public String render() {
			StrCreator sc = new StrCreator();
			sc.o("SELECT ");
			renderFields(sc);
			sc.o(" FROM %s", tblFrag.render());
			return sc.str;
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
		
		public FragmentParser(FactoryService factorySvc, DTypeRegistry registry) {
			super(factorySvc);
			this.registry = registry;
			this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
		}
		
		public void createAlias(AliasedFragment frag) {
			char ch = (char) ('a' + nextAliasIndex++);
			frag.alias = String.format("%c", ch);
		}

		public SelectStatementFragment parseSelect(QuerySpec spec) {
			QueryExp exp = spec.queryExp;
			
			SelectStatementFragment selectFrag = new SelectStatementFragment();
			initFields(spec, selectFrag);
			TableFragment tblFrag = new TableFragment();
			createAlias(tblFrag);
			tblFrag.name = exp.typeName;
			
			selectFrag.tblFrag = tblFrag;
			
			return selectFrag;
		}

		private void initFields(QuerySpec spec, SelectStatementFragment selectFrag) {
			DStructType structType = (DStructType) registry.getType(spec.queryExp.typeName);
			
			QueryType queryType = queryDetectorSvc.detectQueryType(spec);
			switch(queryType) {
			case ALL_ROWS:
//				addWhereExist(sc, spec);
				break;
			case OP:
//				addWhereClauseOp(sc, spec, typeName, tbl, statement);
				break;
			case PRIMARY_KEY:
			default:
				FieldFragment fieldF = new FieldFragment();
				createAlias(fieldF);
				TypePair pair = DValueHelper.findPrimaryKeyFieldPair(structType);
				fieldF.fieldType = pair.type;
				fieldF.name = pair.name;
				fieldF.isStar = true;
				fieldF.structType = structType;
				selectFrag.fieldL.add(fieldF);
				
//				addWhereClausePrimaryKey(sc, spec, spec.queryExp.filter, typeName, tbl, statement);
				break;
			}
		}

		public String render(SelectStatementFragment selectFrag) {
			
			return selectFrag.render();
		}
		
		
		
	}
	

	@Test
	public void test1() {
		String src = buildSrc();
		DeliaDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);
		
		Delia delia = dao.getDelia();
		FactoryService factorySvc = delia.getFactoryService();
		DTypeRegistry registry = dao.getRegistry();
		Runner runner = new RunnerImpl(factorySvc, dao.getDbInterface());
		
		FragmentParser parser = new FragmentParser(factorySvc, registry);
		
		QueryBuilderService queryBuilderSvc = factorySvc.getQueryBuilderService();
		ScalarValueBuilder builder = factorySvc.createScalarValueBuilder(registry);
		DValue dval = builder.buildInt(1);
		
		QueryExp exp = queryBuilderSvc.createPrimaryKeyQuery("Flight", dval);
		QuerySpec spec= queryBuilderSvc.buildSpec(exp, runner);
		
		SelectStatementFragment selectFrag = parser.parseSelect(spec);
		
		String sql = parser.render(selectFrag);
		assertEquals("SELECT Flight as a", sql);
	}
	
	//---

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

}
