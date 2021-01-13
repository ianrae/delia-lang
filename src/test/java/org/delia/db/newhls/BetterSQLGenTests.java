package org.delia.db.newhls;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.db.newhls.cond.FilterCond;
import org.delia.db.newhls.cud.HLDDelete;
import org.delia.db.newhls.cud.HLDDeleteStatement;
import org.delia.db.newhls.cud.HLDUpdate;
import org.delia.db.newhls.cud.HLDUpdateStatement;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.ListWalker;
import org.delia.type.DTypeRegistry;
import org.junit.Test;

public class BetterSQLGenTests extends NewHLSTestBase {

	public static class SimpleBase {
		public SqlColumn tblFrag;
	}
	public static class SimpleSelect extends SimpleBase {
		public List<SqlColumn> fieldL = new ArrayList<>();
		public FilterCond filter;
	}
	public static class SimpleDelete extends SimpleBase {
		public FilterCond filter;
	}
	public static class SimpleUpdate extends SimpleBase {
		public List<SqlColumn> fieldL = new ArrayList<>();
		public FilterCond filter;
	}

	public static class SimpleSqlBuilder {
		public SimpleSelect buildFrom(HLDQuery hld) {
			SimpleSelect sel = new SimpleSelect();
			sel.tblFrag = new SqlColumn(hld.fromAlias, hld.fromType.getName());
			for(HLDField fld: hld.fieldL) {
				SqlColumn ff = new SqlColumn(fld.alias, fld.fieldName);
				sel.fieldL.add(ff);
			}
			sel.filter = hld.filter;
			return sel;
		}
		public SimpleDelete buildFrom(HLDDelete hld) {
			SimpleDelete sel = new SimpleDelete();
			sel.tblFrag = new SqlColumn(hld.hld.fromAlias, hld.hld.fromType.getName());
			sel.filter = hld.hld.filter;
			return sel;
		}
		public SimpleUpdate buildFrom(HLDUpdate hld) {
			SimpleUpdate sel = new SimpleUpdate();
			sel.tblFrag = new SqlColumn(hld.hld.fromAlias, hld.hld.fromType.getName());
			for(HLDField fld: hld.fieldL) {
				SqlColumn ff = new SqlColumn(fld.alias, fld.fieldName);
				sel.fieldL.add(ff);
			}
			sel.filter = hld.hld.filter;
			return sel;
		}
	}

	public static class SimpleSqlGenerator {
		private DTypeRegistry registry;
		private FactoryService factorySvc;
		private SQLWhereGenerator sqlWhereGen;
		private SqlParamGenerator paramGen;

		public SimpleSqlGenerator(DTypeRegistry registry, FactoryService factorySvc) {
			this.registry = registry;
			this.factorySvc = factorySvc;
			this.sqlWhereGen = new SQLWhereGenerator(registry, factorySvc);
			this.paramGen = new SqlParamGenerator(registry, factorySvc); 
		}

		public String gen(SimpleSelect sel) {
			StrCreator sc = new StrCreator();
			sc.o("SELECT ");

			ListWalker<SqlColumn> walker = new ListWalker<>(sel.fieldL);
			while(walker.hasNext()) {
				SqlColumn ff = walker.next();
				sc.o(ff.render());
				walker.addIfNotLast(sc, ", ");
			}

			genTblName(sc, " FROM %s", sel);
			outputWhere(sc, sel.filter);
			return sc.toString();
		}
		private void genTblName(StrCreator sc, String fmt, SimpleBase sel) {
			sc.o(fmt, sel.tblFrag.renderAsTable());
		}

		private void outputWhere(StrCreator sc, FilterCond filter) {
			if (filter != null) {
				SqlStatement stm = new SqlStatement();
				String s = this.sqlWhereGen.doFilter(filter, paramGen, stm);
				sc.o(" WHERE %s", s);
			}
		}

		public String gen(SimpleDelete sel) {
			StrCreator sc = new StrCreator();
			sc.o("DELETE");
			genTblName(sc, " FROM %s", sel);
			outputWhere(sc, sel.filter);
			return sc.toString();
		}
		public String gen(SimpleUpdate sel) {
			StrCreator sc = new StrCreator();
			sc.o("UPDATE");
			genTblName(sc, " %s", sel);
			sc.o(" SET ");
			ListWalker<SqlColumn> walker = new ListWalker<>(sel.fieldL);
			while(walker.hasNext()) {
				SqlColumn ff = walker.next();
				sc.o("%s = %s", ff.render(), "?");
				
				walker.addIfNotLast(sc, ", ");
			}

			outputWhere(sc, sel.filter);
			return sc.toString();
		}
	}


	@Test
	public void testSelect() {
		useCustomer11Src = true;
		String src = "let x = Customer[55]";

		HLDQueryStatement hld = buildFromSrc(src, 0); 
		SimpleSqlBuilder builder = new SimpleSqlBuilder();
		SimpleSelect sel = builder.buildFrom(hld.hldquery);
		
		SimpleSqlGenerator gen = new SimpleSqlGenerator(this.session.getExecutionContext().registry, delia.getFactoryService());
		String sql = gen.gen(sel);
		log.log(sql);
		assertEquals("SELECT t0.cid, t0.x FROM Customer as t0 WHERE t0.cid=?", sql);
	}
	
	@Test
	public void testDelete() {
		useCustomer11Src = true;
		String src = "delete Customer[x > 10]";

		HLDDeleteStatement hld = buildFromSrcDelete(src, 0); 
		SimpleSqlBuilder builder = new SimpleSqlBuilder();
		SimpleDelete sel = builder.buildFrom(hld.hlddelete);
		
		SimpleSqlGenerator gen = new SimpleSqlGenerator(this.session.getExecutionContext().registry, delia.getFactoryService());
		String sql = gen.gen(sel);
		log.log(sql);
		assertEquals("DELETE FROM Customer as t0 WHERE t0.x > ?", sql);
	}
	
	@Test
	public void testpUpdate() {
		useCustomer1NSrc = true;
		String src = "update Customer[1] {x: 45}";
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		SimpleSqlBuilder builder = new SimpleSqlBuilder();
		SimpleUpdate sel = builder.buildFrom(hldupdate.hldupdate);
		
		SimpleSqlGenerator gen = new SimpleSqlGenerator(this.session.getExecutionContext().registry, delia.getFactoryService());
		String sql = gen.gen(sel);
		log.log(sql);
		assertEquals("UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", sql);
	}
	
}
