package org.delia.db.newhls;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.delia.core.FactoryService;
import org.delia.db.newhls.cond.FilterCond;
import org.delia.db.newhls.cond.FilterFunc;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.db.newhls.cond.SingleFilterCond;
import org.delia.db.newhls.cond.SymbolChain;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

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
		
		StringJoiner joiner = generateFields(hld);
		sc.o(joiner.toString());
		
		sc.o(" FROM %s as %s", hld.fromType.getName(), hld.fromAlias);
		
		SqlStatement stm = new SqlStatement();
		generateJoins(sc, hld, stm, paramGen);
		generateWhere(sc, hld, stm, paramGen);
		generateOrderBy(sc, hld);
		stm.sql = sc.toString();
		return stm;
	}

	private void generateOrderBy(StrCreator sc, HLDQuery hld) {
		List<QueryFnSpec> list = hld.funcL.stream().filter(x -> x.isFn("orderBy")).collect(Collectors.toList());
		if (list.isEmpty()) {
			return;
		}
		
		sc.o(" ORDER BY");
		for(QueryFnSpec fnspec: list) {
			JoinElement el = hld.findJoinByAlias(fnspec.structField.alias, hld);
			if (el != null && el.relinfo.isParent) {
				//need to reverse, since parent doesn't have child id
				TypePair pkpair = el.getOtherSidePK();
				String parentName = el.getOtherSideField(); //TODO. can otherSide ever be null??
				sc.o(" ON %s.%s=%s.%s", el.srcAlias, pkpair.name, el.aliasName, parentName);  
			} else {
				sc.o(" %s.%s", fnspec.structField.alias, fnspec.structField.fieldName);
			}
		}
	}


	private void generateJoins(StrCreator sc, HLDQuery hld, SqlStatement stm, SqlParamGenerator paramGen) {
		for(JoinElement el: hld.joinL) {
			if (el.relinfo.isManyToMany()) {
				//TODO
			} else {
				//JOIN Address as t1 ON t0.id=t1.cust
				String tbl = el.relationField.fieldType.getName();
				sc.o(" JOIN %s as %s", tbl, el.aliasName);
				
				if (el.relinfo.isParent) {
					//need to reverse, since parent doesn't have child id
					TypePair pkpair = el.getOtherSidePK();
					String parentName = el.getOtherSideField(); //TODO. can otherSide ever be null??
					sc.o(" ON %s.%s=%s.%s", el.srcAlias, pkpair.name, el.aliasName, parentName);  
				} else {
					TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(el.relationField.fieldType);
					sc.o(" ON %s.%s=%s.%s", el.srcAlias, el.relationField.fieldName, el.aliasName, pkpair.name);  
				}
			}
		}
	}


	private StringJoiner generateFields(HLDQuery hld) {
		StringJoiner joiner = new StringJoiner(",");
		Optional<QueryFnSpec> optCountFn = hld.funcL.stream().filter(x -> x.filterFn.fnName.equals("count")).findAny();
		if (optCountFn.isPresent()) {
			joiner.add("count(*)");
			return joiner;
		}
		
		for(HLDField rf: hld.fieldL) {
			if (rf.asStr != null) {
				joiner.add(String.format("%s.%s as %s", rf.alias, rf.fieldName, rf.asStr));
			} else {
				joiner.add(String.format("%s.%s", rf.alias, rf.fieldName));
			}
		}
		return joiner;
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
			String not = ofc.isNot ? "NOT " : "";
			fragment = String.format("%s%s %s %s", not, s1, ofc.op.op, s2);
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
			return String.format("%s.%s", val1.alias, chain.list.get(0)); //TODO: later support list > 1
		}
		case FUNCTION:
		default:
			throw new HLDException("renderVal not impl1");
		}
	}

}