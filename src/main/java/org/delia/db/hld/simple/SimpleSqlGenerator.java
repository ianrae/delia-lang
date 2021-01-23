package org.delia.db.hld.simple;

import org.delia.core.FactoryService;
import org.delia.db.hld.SQLWhereGenerator;
import org.delia.db.hld.SqlColumn;
import org.delia.db.hld.SqlParamGenerator;
import org.delia.db.hld.cond.FilterCond;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.ListWalker;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
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
	
	public String genAny(SimpleBase simple, SqlStatement stm) {
		if (simple instanceof SimpleSelect) {
			return gen((SimpleSelect)simple, stm);
		} else if (simple instanceof SimpleDelete) {
			return gen((SimpleDelete)simple, stm);
		} else if (simple instanceof SimpleUpdate) {
			return gen((SimpleUpdate)simple, stm);
		} else if (simple instanceof SimpleInsert) {
			return gen((SimpleInsert)simple, stm);
		} else {
			DeliaExceptionHelper.throwError("unknown-simplesql", "unknown simple sql type");
			return null;
		}
	}
	public String gen(SimpleSelect simple, SqlStatement stm) {
		StrCreator sc = new StrCreator();
		sc.o("SELECT ");

		ListWalker<SqlColumn> walker = new ListWalker<>(simple.fieldL);
		while(walker.hasNext()) {
			SqlColumn ff = walker.next();
			sc.o(ff.render());
			walker.addIfNotLast(sc, ", ");
		}

		genTblName(sc, " FROM %s", simple);
		outputWhere(sc, simple.filter, stm);
		return sc.toString();
	}
	private void genTblName(StrCreator sc, String fmt, SimpleBase sel) {
		sc.o(fmt, sel.tblFrag.renderAsTable());
	}

	private void outputWhere(StrCreator sc, FilterCond filter, SqlStatement stm) {
		if (filter != null) {
			String s = this.sqlWhereGen.doFilter(filter, paramGen, stm);
			sc.o(" WHERE %s", s);
		}
	}

	public String gen(SimpleDelete simple, SqlStatement stm) {
		StrCreator sc = new StrCreator();
		sc.o("DELETE");
		genTblName(sc, " FROM %s", simple);
		outputWhere(sc, simple.filter, stm);
		return sc.toString();
	}
	public String gen(SimpleUpdate simple, SqlStatement stm) {
		StrCreator sc = new StrCreator();
		sc.o("UPDATE");
		genTblName(sc, " %s", simple);
		sc.o(" SET ");
		ListWalker<SqlColumn> walker = new ListWalker<>(simple.fieldL);
		int index = 0;
		while(walker.hasNext()) {
			SqlColumn ff = walker.next();
			sc.o("%s = %s", ff.render(), "?");
			
			walker.addIfNotLast(sc, ", ");
			DValue inner = simple.hld.valueL.get(index++);
			stm.paramL.add(inner);
		}

		outputWhere(sc, simple.filter, stm);
		return sc.toString();
	}
	public String gen(SimpleInsert simple, SqlStatement stm) {
		StrCreator sc = new StrCreator();
		sc.o("INSERT INTO");
		genTblName(sc, " %s", simple);
		
		sc.o(" (");
		ListWalker<SqlColumn> walker = new ListWalker<>(simple.fieldL);
		int index = 0;
		while(walker.hasNext()) {
			SqlColumn ff = walker.next();
			sc.o("%s", ff.render());
			walker.addIfNotLast(sc, ", ");
		}
		sc.o(")");
		
		sc.o(" VALUES(");
		walker = new ListWalker<>(simple.fieldL);
		index = 0;
		while(walker.hasNext()) {
			SqlColumn ff = walker.next();
			sc.o("%s", "?");
			
			walker.addIfNotLast(sc, ", ");
			DValue inner = simple.hld.valueL.get(index++);
			stm.paramL.add(inner);
		}
		sc.o(")");

		return sc.toString();
	}
	
}