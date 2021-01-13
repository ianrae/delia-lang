package org.delia.db.newhls;

import org.delia.core.FactoryService;
import org.delia.db.newhls.cond.FilterCond;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.OpAndOrFilter;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.db.newhls.cond.SingleFilterCond;
import org.delia.db.newhls.cond.SymbolChain;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

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
			String valsql = renderValParam(sfc, paramGen, stm);
			return String.format("%s.%s=%s", alias, fieldName, valsql);
		} else if (filter instanceof OpFilterCond) {
			OpFilterCond ofc = (OpFilterCond) filter;
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


	private String renderValParam(SingleFilterCond sfc, SqlParamGenerator paramGen, SqlStatement stm) {
		if (paramGen == null) {
			return sfc.renderSql();
		} else {
			DValue dval = paramGen.convert(sfc.val1);
			stm.paramL.add(dval);
			return "?";
		}
	}
	private String renderVal(FilterVal val1, SqlParamGenerator paramGen, SqlStatement stm) {
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
			return String.format("%s.%s", val1.alias, val1.structField.fieldName);
		case SYMBOLCHAIN:
		{
			SymbolChain chain = val1.asSymbolChain();
			if (chain.el != null && chain.el.aliasNameAdditional != null) {
				return String.format("%s.%s", chain.el.aliasNameAdditional, chain.list.get(0)); //TODO: later support list > 1
			} else {
				return String.format("%s.%s", val1.alias, chain.list.get(0)); //TODO: later support list > 1
			}
		}
		case FUNCTION:
		default:
			throw new HLDException("renderVal not impl1");
		}
	}
}