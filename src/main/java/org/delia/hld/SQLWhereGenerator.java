package org.delia.hld;

import java.util.Arrays;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.db.SqlStatement;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;
import org.delia.hld.cond.FilterCond;
import org.delia.hld.cond.FilterFunc;
import org.delia.hld.cond.FilterVal;
import org.delia.hld.cond.InFilterCond;
import org.delia.hld.cond.OpAndOrFilter;
import org.delia.hld.cond.OpFilterCond;
import org.delia.hld.cond.SingleFilterCond;
import org.delia.hld.cond.SymbolChain;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

/**
 * @author ian
 *
 */
public class SQLWhereGenerator {
	private DTypeRegistry registry;
	private FactoryService factorySvc;

	public SQLWhereGenerator(DTypeRegistry registry, FactoryService factorySvc) {
		this.registry = registry;
		this.factorySvc = factorySvc;
	}
	
	public String generateSqlWhere(HLDQuery hld, SqlStatement stm) {
		SqlParamGenerator paramGen = new SqlParamGenerator(registry, factorySvc); 
		String sql = generateWhereClause(hld, stm, paramGen);
		if (sql == null) {
			return null;
		}
		return " " + sql;
	}
	
	private String generateWhereClause(HLDQuery hld, SqlStatement stm, SqlParamGenerator paramGen) {
		FilterCond filter = hld.filter;
		String fragment = doFilter(filter, paramGen, stm);
		return fragment;
	}

	public String doFilter(FilterCond filter, SqlParamGenerator paramGen, SqlStatement stm) {
		if (filter instanceof SingleFilterCond) {
			SingleFilterCond sfc = (SingleFilterCond) filter;
			if (sfc.isAllQuery()) {
				return null;
			}
			String alias = sfc.val1.alias;
			String fieldName = sfc.val1.structField.fieldName;
			String valsql = renderSingleParam(sfc, paramGen, stm);
			return String.format("%s=%s", renderTerm(alias, fieldName), valsql);
		} else if (filter instanceof OpFilterCond) {
			OpFilterCond ofc = (OpFilterCond) filter;
			if (ofc.customRenderer != null) {
				return ofc.customRenderer.render(ofc, paramGen, stm);
			}
			String s1 = renderVal(ofc.val1, paramGen, stm);
			String s2 = renderVal(ofc.val2, paramGen, stm);
			String not = ofc.isNot ? "NOT " : "";
			String op = opToSql(ofc.op.op);
			return String.format("%s%s %s %s", not, s1, op, s2);
		} else if (filter instanceof OpAndOrFilter) {
			OpAndOrFilter ofc = (OpAndOrFilter) filter;
			String s1 = doFilter(ofc.cond1, paramGen, stm); //** recursion **
			String s2 = doFilter(ofc.cond2, paramGen, stm); //** recursion **
			String and = ofc.isAnd ? "AND" : "OR";
			return String.format("%s %s %s", s1, and, s2);
		} else if (filter instanceof InFilterCond) {
			InFilterCond ifc = (InFilterCond) filter;
			String s1 = renderVal(ifc.val1, paramGen, stm);
			
			StrCreator sc = new StrCreator();
			sc.o("%s IN (", s1);
			ListWalker<FilterVal> walker = new ListWalker<>(ifc.list);
			while(walker.hasNext()) {
				FilterVal ff = walker.next();
				String s = renderVal(ff, paramGen, stm);
				sc.o(s);
				walker.addIfNotLast(sc, ", ");
			}
			sc.o(")");
			return sc.toString();
		} else {
			return null;
		}
	}


	private String opToSql(String op) {
		switch(op) {
		case "==":
			return "=";
		case "!=":
			return "<>";
		case "like":
			return "LIKE";
		default:
			return op;
		}
	}


	private String renderSingleParam(SingleFilterCond sfc, SqlParamGenerator paramGen, SqlStatement stm) {
		if (paramGen == null) {
			return sfc.renderSql();
		} else {
			DValue dval = paramGen.convert(sfc.val1);
			stm.paramL.add(dval);
			return "?";
		}
	}
	private String renderVal(FilterVal val1, SqlParamGenerator paramGen, SqlStatement stm) {
		if (val1.customRenderer != null) {
			return val1.customRenderer.render(val1, paramGen, stm);
		}
		boolean notParam = val1.isFn() || val1.isSymbol() || val1.isSymbolChain();
		if (paramGen == null || notParam) {
			return doRenderVal(val1);
		} else {
			DValue dval = paramGen.convert(val1);
			stm.paramL.add(dval);
			return "?";
		}
	}

	private String doRenderVal(FilterVal val1) {
		switch(val1.valType) {
		case BOOLEAN:
		case INT:
		case LONG:
		case NUMBER:
			return val1.exp.strValue();
		case STRING:
			return String.format("'%s'", val1.exp.strValue());
		case SYMBOL:
			return renderTerm(val1.alias, val1.structField.fieldName);
		case NULL:
			return "null";
		case SYMBOLCHAIN:
		{
			SymbolChain chain = val1.asSymbolChain();
			if (chain.el != null && chain.el.aliasNameAdditional != null) {
				return renderTerm(chain.el.aliasNameAdditional, chain.list.get(0)); //TODO: later support list > 1
			} else {
				return renderTerm(val1.alias, chain.list.get(0)); //TODO: later support list > 1
			}
		}
		case FUNCTION:
		{
			FilterFunc fn = val1.asFunc();
			return genFnSQL(val1, fn);
		}
		default:
			throw new HLDException("renderVal not impl1");
		}
	}

	private String genFnSQL(FilterVal val1, FilterFunc fn) {
		List<String> allFns = Arrays.asList("year", "month", "day", "hour", "minute", "second");

		//DATEPART(year, '2017/08/25')
		if (allFns.contains(fn.fnName)) {
			boolean useDatePart = false; //H2 not support DATEPART
			if (useDatePart) {
				String fieldName = val1.exp.strValue();
				String ss = val1.alias == null ? fieldName : renderTerm(val1.alias, fieldName);
				String s = String.format("DATEPART(%s,%s)", fn.fnName, ss);
				return s;
			} else { //SELECT EXTRACT(MONTH FROM "2017-06-15");
				String fieldName = val1.exp.strValue();
				String ss = val1.alias == null ? fieldName : renderTerm(val1.alias, fieldName);
				String s = String.format("EXTRACT(%s FROM %s)", fn.fnName.toUpperCase(), ss);
				return s;
			}
		} else {
			DeliaExceptionHelper.throwNotImplementedError("unknown filter fn '%s'", fn.fnName);
			return null;
		}
	}

	private String renderTerm(String alias, String fieldName) {
		if (alias == null) {
			return fieldName;
		}
		return String.format("%s.%s", alias, fieldName);
	}
}