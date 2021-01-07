package org.delia.db.newhls;

import java.util.StringJoiner;

import org.delia.db.newhls.cond.FilterCond;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.db.newhls.cond.SingleFilterCond;
import org.delia.db.sql.StrCreator;

/**
 * Generate SQL statement from an HLDQuery
 * @author ian
 *
 */
public class HLDSQLGenerator {
	
	public String generateSQL(HLDQuery hld) {
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
		
		generateWhere(sc, hld);
		return sc.toString();
	}

	private void generateWhere(StrCreator sc, HLDQuery hld) {
		FilterCond filter = hld.filter;
		String fragment = null;
		if (filter instanceof SingleFilterCond) {
			SingleFilterCond sfc = (SingleFilterCond) filter;
			String alias = sfc.val1.alias;
			String fieldName = sfc.val1.structField.fieldName;
			fragment = String.format("%s.%s=%s", alias, fieldName, sfc.renderSql());
		} else if (filter instanceof OpFilterCond) {
			OpFilterCond ofc = (OpFilterCond) filter;
			String s1 = renderVal(ofc.val1);
			String s2 = renderVal(ofc.val2);
			fragment = String.format("%s %s %s", s1, ofc.op.op, s2);
		}
		sc.o(" WHERE %s", fragment);
	}

	private String renderVal(FilterVal val1) {
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