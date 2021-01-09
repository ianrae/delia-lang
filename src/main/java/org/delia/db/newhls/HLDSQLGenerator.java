package org.delia.db.newhls;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.newhls.cond.FilterCond;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.db.newhls.cond.SingleFilterCond;
import org.delia.db.newhls.cond.SymbolChain;
import org.delia.db.newhls.cud.HLDDelete;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.type.DStructType;
import org.delia.type.DType;
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
	private DatIdMap datIdMap;
	private SqlColumnBuilder columnBuilder;

	public HLDSQLGenerator(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap) {
		this.registry = registry;
		this.factorySvc = factorySvc;
		this.datIdMap = datIdMap;
		this.columnBuilder = new SqlColumnBuilder(datIdMap);
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
			if (el != null && el.relinfo.notContainsFK()) {
				if (el.relinfo.isManyToMany()) {
					//passing null here ok because we know its M:M
//					SqlColumn npair = doMapFieldIfNeeded(el.aliasName, null, fnspec.structField.dtype, el);
					DType fieldType = DValueHelper.findFieldType(fnspec.structField.dtype, fnspec.structField.fieldName);
					SqlColumn npair = doMapFieldScalarOrRef(fieldType, el.aliasName, fnspec.structField.fieldName, fnspec.structField.dtype, el);
					sc.o(" %s", npair.toString());  
				} else {
					//need to reverse, since parent doesn't have child id
					String parentName = el.getOtherSideField(); //TODO. can otherSide ever be null??
					sc.o(" %s.%s", el.aliasName, parentName);  
				}
			} else {
				sc.o(" %s.%s", fnspec.structField.alias, fnspec.structField.fieldName);
			}
		}
	}


	private void generateJoins(StrCreator sc, HLDQuery hld, SqlStatement stm, SqlParamGenerator paramGen) {
		for(JoinElement el: hld.joinL) {
			if (el.relinfo.isManyToMany()) {
				//select t0.cid,t0.x,t1.leftv,t1.rightv from Customers as t0 join cat on t1 as t0.cid=cat.leftv where t0.cid=?
				String tbl = datIdMap.getAssocTblName(el.relinfo.getDatId());
				sc.o(" JOIN %s as %s", tbl, el.aliasName);
				
				String field1 = datIdMap.getAssocFieldFor(el.relinfo);
//				String field2 = datIdMap.getAssocOtherField(el.relinfo);
				TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(el.relinfo.nearType);
				
				sc.o(" ON %s.%s=%s.%s", el.srcAlias, pkpair.name, el.aliasName, field1);  
				if (el.aliasNameAdditional != null) {
					doSimpleJoin(sc, el, el.aliasNameAdditional);
				}
			} else {
				doSimpleJoin(sc, el, el.aliasName);
			}
		}
	}
	private void doSimpleJoin(StrCreator sc, JoinElement el, String alias) {
		//JOIN Address as t1 ON t0.id=t1.cust
		String tbl = el.relationField.fieldType.getName();
		sc.o(" JOIN %s as %s", tbl, alias);
		
		if (el.relinfo.isParent) {
			//need to reverse, since parent doesn't have child id
			TypePair pkpair = el.getThisSidePK();
			String parentName = el.getOtherSideField(); //TODO. can otherSide ever be null??
			sc.o(" ON %s.%s=%s.%s", el.srcAlias, pkpair.name, alias, parentName);  
		} else {
			SqlColumn npair = doMapFieldIfNeeded(el.srcAlias, el.relationField.fieldName, el.relationField.dtype, el);
			TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(el.relationField.fieldType);
			sc.o(" ON %s=%s.%s", npair.toString(), alias, pkpair.name);  
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
			SqlColumn npair = mapFieldIfNeeded(rf);
			if (rf.asStr != null) {
				joiner.add(String.format("%s as %s", npair.toString(), rf.asStr));
			} else {
				joiner.add(String.format("%s", npair.toString()));
			}
		}
		return joiner;
	}


	private SqlColumn mapFieldIfNeeded(HLDField rf) {
		if (rf.source instanceof JoinElement) {
			JoinElement el = (JoinElement) rf.source;
			return doMapFieldScalarOrRef(rf.fieldType, rf.alias, rf.fieldName, rf.structType, el);
		} else {
			return new SqlColumn(rf.alias, rf.fieldName);
		}
	}
	private SqlColumn doMapFieldScalarOrRef(DType fieldType, String alias, String fieldName, DStructType structType, JoinElement el) {
		if (fieldType.isStructShape()) {
			return doMapFieldIfNeeded(alias, fieldName, structType, el);
		} else {
			return columnBuilder.adjustScalar(alias, fieldName, structType, el);
		}
	}
	private SqlColumn doMapFieldIfNeeded(String alias, String fieldName, DStructType structType, JoinElement el) {
		return columnBuilder.adjust(alias, structType, fieldName, el);
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


	public SqlStatement generateSqlStatement(HLDDelete hlddel) {
		StrCreator sc = new StrCreator();
		sc.o("DELETE FROM ");
		
		HLDQuery hld = hlddel.hld;
		sc.o("%s as %s", hld.fromType.getName(), hld.fromAlias);
		
		SqlParamGenerator paramGen = new SqlParamGenerator(registry, factorySvc); 
		SqlStatement stm = new SqlStatement();
		generateWhere(sc, hld, stm, paramGen);
		stm.sql = sc.toString();
		return stm;
	}

}