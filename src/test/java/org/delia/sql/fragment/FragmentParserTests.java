package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
import org.delia.db.memdb.MemDBInterface;
import org.delia.runner.ResultValue;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
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
			return String.format("%s as %s", alias, name);
		}
	}
	
	public static class SelectStatementFragment implements SqlFragment {
		private List<SqlFragment> fieldL = new ArrayList<>();
		public SqlFragment tblFrag;
		
		
		@Override
		public String render() {
			String sql = String.format("SELECT %s", tblFrag.render());
			return sql;
		}
	}
	
	public static class FragmentParser extends ServiceBase {
		private int nextAliasIndex = 0;
		
		public FragmentParser(FactoryService factorySvc) {
			super(factorySvc);
		}
		
		public void createAlias(AliasedFragment frag) {
			char ch = (char) ('a' + nextAliasIndex++);
			frag.alias = String.format("%c", ch);
		}

		public SelectStatementFragment parseSelect(QueryExp spec) {
			
			SelectStatementFragment selectFrag = new SelectStatementFragment();
			
			AliasedFragment tblFrag = new AliasedFragment();
			createAlias(tblFrag);
			tblFrag.name = spec.typeName;
			
			selectFrag.tblFrag = tblFrag;
			
			return selectFrag;
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
		DTypeRegistry registry = dao.getRegistry();
		FactoryService factorySvc = dao.getDelia().getFactoryService();
		
		FragmentParser parser = new FragmentParser(factorySvc);
		
		QueryBuilderService queryBuilderSvc = factorySvc.getQueryBuilderService();
		ScalarValueBuilder builder = factorySvc.createScalarValueBuilder(registry);
		DValue dval = builder.buildInt(1);
		
		QueryExp spec = queryBuilderSvc.createPrimaryKeyQuery("Fight", dval);
		
		SelectStatementFragment selectFrag = parser.parseSelect(spec);
		
		String sql = parser.render(selectFrag);
		assertEquals("sdf", sql);
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
