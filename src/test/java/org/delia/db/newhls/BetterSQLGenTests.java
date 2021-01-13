package org.delia.db.newhls;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.delia.core.FactoryService;
import org.delia.db.newhls.cond.FilterCond;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.ListWalker;
import org.delia.type.DTypeRegistry;
import org.junit.Test;

public class BetterSQLGenTests extends NewHLSTestBase {

	public static class AFrag { //implements SqlFragment {
		public String alias;
		public String name;

		public AFrag() {
		}
		public AFrag(String alias, String name) {
			this.alias = alias;
			this.name = name;
		}

		public String render() {
			if (StringUtils.isEmpty(alias)) {
				return name;
			}
			return String.format("%s.%s", alias, name);
		}

		//		public String renderAsAliasedFrag() {
		//			if (StringUtils.isEmpty(alias)) {
		//				return name;
		//			}
		//			return String.format("%s.%s", alias, name);
		//		}
		//		@Override
		//		public int getNumSqlParams() {
		//			if (name == null) {
		//				return 0;
		//			}
		//			return name.contains("?") ? 1 : 0; 
		//		}
	}	

	public static class LSelect {
		public AFrag tblFrag;
		public List<AFrag> fieldL = new ArrayList<>();
		public FilterCond filter;
	}

	public static class LBuilder {
		public LSelect buildFrom(HLDQuery hld) {
			LSelect sel = new LSelect();
			sel.tblFrag = new AFrag(hld.fromAlias, hld.fromType.getName());
			for(HLDField fld: hld.fieldL) {
				AFrag ff = new AFrag(fld.alias, fld.fieldName);
				sel.fieldL.add(ff);
			}
			sel.filter = hld.filter;
			return sel;
		}
	}

	public static class LowSqlGen {
		private DTypeRegistry registry;
		private FactoryService factorySvc;
		private SQLWhereGenerator sqlWhereGen;
		private SqlParamGenerator paramGen;

		public LowSqlGen(DTypeRegistry registry, FactoryService factorySvc) {
			this.registry = registry;
			this.factorySvc = factorySvc;
			this.sqlWhereGen = new SQLWhereGenerator(registry, factorySvc);
			this.paramGen = new SqlParamGenerator(registry, factorySvc); 
		}
		

		public String gen(LSelect sel) {
			StrCreator sc = new StrCreator();
			sc.o("SELECT ");

			ListWalker<AFrag> walker = new ListWalker<>(sel.fieldL);
			while(walker.hasNext()) {
				AFrag ff = walker.next();
				sc.o(ff.render());
				walker.addIfNotLast(sc, ", ");
			}

			if (sel.filter != null) {
				SqlStatement stm = new SqlStatement();
				String s = this.sqlWhereGen.doFilter(sel.filter, paramGen, stm);
				sc.o(" WHERE %s", s);
			}

			return sc.toString();
		}
	}


	//		sc.o("(SELECT %s.cid FROM %s as %s WHERE", alias, hld.fromType.getName(), alias);
	@Test
	public void test() {
		useCustomer11Src = true;
		String src = "let x = Customer[55]";

		HLDQueryStatement hld = buildFromSrc(src, 0); 
		LBuilder builder = new LBuilder();
		LSelect sel = builder.buildFrom(hld.hldquery);
		
		LowSqlGen gen = new LowSqlGen(this.session.getExecutionContext().registry, delia.getFactoryService());
		String sql = gen.gen(sel);
		log.log(sql);
		assertEquals("SELECT t0.cid, t0.x WHERE t0.cid=?", sql);
	}
}
