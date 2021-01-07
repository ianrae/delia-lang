package org.delia.db.newhls;

import java.util.StringJoiner;

import org.delia.core.FactoryService;
import org.delia.db.newhls.cond.FilterCond;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.db.newhls.cond.SingleFilterCond;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

/**
 * Generate SQL statement from an HLDQuery
 * @author ian
 *
 */
public class HLDSQLGenerator {
	private DTypeRegistry registry;
	private FactoryService factorySvc;

	public HLDSQLGenerator(DTypeRegistry registry, FactoryService factorySvc) {
		this.registry = registry;
		this.factorySvc = factorySvc;
	}

	
	public SqlStatement generateSqlStatement(HLDQuery hld) {
		SqlParamGenerator paramGen = new SqlParamGenerator(registry, factorySvc); 
		return doGenerateSql(hld, paramGen);
	}
	public String generateRawSql(HLDQuery hld) {
		SqlStatement stm = doGenerateSql(hld, null);
		return stm.sql;
	}
	
	private SqlStatement doGenerateSql(HLDQuery hld, SqlParamGenerator paramGen) {
		StrCreator sc = new StrCreator();
		sc.o("SELECT ");
		
		StringJoiner joiner = new StringJoiner(",");
		for(HLDField rf: hld.fieldL) {
			if (rf.asStr != null) {
				joiner.add(String.format("%s.%s as %s", rf.alias, rf.fieldName, rf.asStr));
			} else {
				joiner.add(String.format("%s.%s", rf.alias, rf.fieldName));
			}
		}
		sc.o(joiner.toString());
		
		sc.o(" FROM %s as %s", hld.fromType.getName(), hld.fromAlias);
		
		SqlStatement stm = new SqlStatement();
		generateWhere(sc, hld, stm, paramGen);
		stm.sql = sc.toString();
		return stm;
	}

	private void generateWhere(StrCreator sc, HLDQuery hld, SqlStatement stm, SqlParamGenerator paramGen) {
		FilterCond filter = hld.filter;
		String fragment = null;
		if (filter instanceof SingleFilterCond) {
			SingleFilterCond sfc = (SingleFilterCond) filter;
			String alias = sfc.val1.alias;
			String fieldName = sfc.val1.structField.fieldName;
			String valsql = renderValParam(sfc, paramGen, stm);
			fragment = String.format("%s.%s=%s", alias, fieldName, valsql);
		} else if (filter instanceof OpFilterCond) {
			OpFilterCond ofc = (OpFilterCond) filter;
			String s1 = renderVal(ofc.val1, paramGen, stm);
			String s2 = renderVal(ofc.val2, paramGen, stm);
			fragment = String.format("%s %s %s", s1, ofc.op.op, s2);
		}
		sc.o(" WHERE %s", fragment);
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
		boolean notParam = val1.isFn() || val1.isSymbol();
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
			
		case FUNCTION:
		default:
			throw new HLDException("renderVal not impl1");
		}
	}

}