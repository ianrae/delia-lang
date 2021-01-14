package org.delia.db.newhls.simple;

import org.delia.core.FactoryService;
import org.delia.db.newhls.SQLWhereGenerator;
import org.delia.db.newhls.SqlColumn;
import org.delia.db.newhls.SqlParamGenerator;
import org.delia.db.newhls.cond.FilterCond;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.ListWalker;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;

/**
 * Renders simple SQL statements
 * @author ian
 *
 */
public class SimpleSqlGenerator {
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
	
	public String genAny(SimpleBase simple) {
		if (simple instanceof SimpleSelect) {
			return gen((SimpleSelect)simple);
		} else if (simple instanceof SimpleDelete) {
			return gen((SimpleDelete)simple);
		} else if (simple instanceof SimpleUpdate) {
			return gen((SimpleUpdate)simple);
		} else {
			DeliaExceptionHelper.throwError("unknown-simplesql", "unknown simple sql type");
			return null;
		}
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