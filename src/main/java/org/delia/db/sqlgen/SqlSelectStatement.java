package org.delia.db.sqlgen;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.hld.HLDField;
import org.delia.db.hld.HLDQuery;
import org.delia.db.hld.JoinElement;
import org.delia.db.hld.QueryFnSpec;
import org.delia.db.hld.SqlColumn;
import org.delia.db.hld.SqlColumnBuilder;
import org.delia.db.hld.SqlParamGenerator;
import org.delia.db.hld.StructField;
import org.delia.db.hld.StructFieldOpt;
import org.delia.db.newhls.cud.TypeOrTable;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

public class SqlSelectStatement implements SqlStatementGenerator {

	private DatIdMap datIdMap;
	private SqlTableNameClause tblClause;
	private SqlWhereClause whereClause;
	private SqlColumnBuilder columnBuilder;
	private SqlParamGenerator paramGen;
	
	private HLDQuery hld;

	public SqlSelectStatement(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap, SqlTableNameClause tblClause, SqlWhereClause whereClause) {
		this.datIdMap = datIdMap;
		this.tblClause = tblClause;
		this.whereClause = whereClause;
		
		this.columnBuilder = new SqlColumnBuilder(datIdMap);
		this.paramGen = new SqlParamGenerator(registry, factorySvc); 
	}

	public void init(HLDQuery hld) {
		this.hld = hld;
		TypeOrTable typeOrTbl = new TypeOrTable(hld.fromType);
		typeOrTbl.alias = hld.fromAlias;
		tblClause.init(typeOrTbl);
		whereClause.init(hld);
	}
	
	public void disableSqlParameterGen() {
		paramGen = null; 
		whereClause.disableSqlParameterGen();
	}
	
	@Override
	public SqlStatement render() {
		SqlStatement stm = new SqlStatement(hld);
		StrCreator sc = new StrCreator();
		sc.o("SELECT ");
		if (hld.hasFn("first") || hld.hasFn("last")) {
			sc.addStr("TOP 1 ");
		}
		if (hld.hasFn("distinct")) {
			sc.addStr("DISTINCT ");
		}
		
		StringJoiner joiner = generateFields(hld);
		sc.o(joiner.toString());

		sc.o(" FROM %s as %s", hld.fromType.getName(), hld.fromAlias);

		generateJoins(sc, hld, stm, paramGen);
		generateWhere(sc, stm);
		generateOrderBy(sc, hld);
		
		if (hld.hasFn("limit")) { //TODO what if more than one limit??
			QueryFnSpec fn = hld.findFn("limit").get();
			Integer n = getArgAsInt(fn, 0); 
			sc.o(" LIMIT %s", n.toString());
		}
		if (hld.hasFn("offset")) { //TODO what if more than one offset??
			QueryFnSpec fn = hld.findFn("offset").get();
			Integer n = getArgAsInt(fn, 0); 
			sc.o(" OFFSET %s", n.toString());
		}
		if (hld.hasFn("ith")) { //TODO what if more than one ith??
			QueryFnSpec fn = hld.findFn("ith").get();
			Integer n = getArgAsInt(fn, 0); 
			sc.o(" LIMIT 1 OFFSET %s", n.toString());
		}
		
		
		stm.sql = sc.toString();
		return stm;
	}


	////==

	private Integer getArgAsInt(QueryFnSpec fn, int i) {
		if (i >= fn.filterFn.argL.size()) {
			DeliaExceptionHelper.throwError("queryfn-bad-index", "missing param in '%s'", fn.filterFn.fnName);
		}
		Integer n = fn.filterFn.argL.get(0).asInt();
		return n;
	}

	//		public String generateSqlWhere(HLDQuery hld, SqlStatement stm) {
	//			return sqlWhereGen.generateSqlWhere(hld, stm);
	//		}
	//		
	private void generateOrderBy(StrCreator sc, HLDQuery hld) {
		List<QueryFnSpec> list = hld.funcL.stream().filter(x -> x.isFn("orderBy")).collect(Collectors.toList());
		if (list.isEmpty()) {
			addOrderByForLast(sc, hld, false);
			return;
		}
		
		String orderByLastFieldName = findOrderByForLast(hld);
		String logicalFieldName = null;
		
		sc.o(" ORDER BY");
		for(QueryFnSpec fnspec: list) {
			logicalFieldName = fnspec.structField.fieldName;
			JoinElement el = hld.findJoinByAlias(fnspec.structField.alias, hld);
			if (el != null && el.relinfo.notContainsFKOrIsManyToMany()) {
				if (el.relinfo.isManyToMany()) {
					//passing null here ok because we know its M:M
					//						SqlColumn npair = doMapFieldIfNeeded(el.aliasName, null, fnspec.structField.dtype, el);
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
			
			String asc = null;
			if (orderByLastFieldName != null && orderByLastFieldName.equals(logicalFieldName)) {
				asc = "desc";
			}
			if (asc == null && fnspec.filterFn.argL.size() > 1) {
				asc = fnspec.filterFn.argL.get(1).asString();
			}
			
			if (asc != null) {
				sc.o(" %s", asc);  
			}
		}
		
		addOrderByForLast(sc, hld, true);
	}

	private String findOrderByForLast(HLDQuery hld) {
		if (!hld.hasFn("last")) return null;
		
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(hld.fromType);
		return pkpair == null ? null : pkpair.name;
	}
	private void addOrderByForLast(StrCreator sc, HLDQuery hld, boolean atLeastOne) {
		if (!hld.hasFn("last")) return;
		
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(hld.fromType);
		if (! atLeastOne) {
			if (pkpair != null) {
				SqlColumn col = new SqlColumn(hld.fromAlias, pkpair.name);
				sc.o(" ORDER BY %s desc", col.render());
			}
			//else nothing to order by, so just do top 1...hmmm
			//TODO: fix if no pkpair then TOP 1 will get first not last. oops. fix
		} else {
			SqlColumn col = new SqlColumn(hld.fromAlias, pkpair.name);
			sc.o(", %s desc", col.render());  
		}
	}
	
	
	private void generateJoins(StrCreator sc, HLDQuery hld, SqlStatement stm, SqlParamGenerator paramGen) {
		for(JoinElement el: hld.joinL) {
			if (el.relinfo.isManyToMany()) {
				//select t0.cid,t0.x,t1.leftv,t1.rightv from Customers as t0 join cat on t1 as t0.cid=cat.leftv where t0.cid=?
				String tbl = datIdMap.getAssocTblName(el.relinfo.getDatId());
				sc.o(" LEFT JOIN %s as %s", tbl, el.aliasName);

				String field1 = datIdMap.getAssocFieldFor(el.relinfo);
				//					String field2 = datIdMap.getAssocOtherField(el.relinfo);
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
		//LEFT JOIN so get nulls
		String tbl = el.relationField.fieldType.getName();
		sc.o(" LEFT JOIN %s as %s", tbl, alias);

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
		Optional<QueryFnSpec> optCountFn = hld.findFn("count"); //stream().filter(x -> x.filterFn.fnName.equals("count")).findAny();
		if (optCountFn.isPresent()) {
			QueryFnSpec qfn = optCountFn.get();
			if (qfn.structField.fieldName == null) {
				joiner.add("count(*)");
			} else {
				Optional<HLDField> opt = hld.fieldL.stream().filter(x -> isStructMatch(x,qfn.structField)).findAny();
				String alias = opt.isPresent() ? opt.get().alias : "t99"; //TODO throw exception later
				SqlColumn col = new SqlColumn(alias, qfn.structField.fieldName);
				joiner.add(String.format("count(%s)", col.render()));
			}
			return joiner;
		}
		
		for(HLDField ff: hld.fieldL) {
			SqlColumn npair = mapFieldIfNeeded(ff);
			npair = addFuncs(npair, ff, hld);
			if (ff.asStr != null) {
				joiner.add(String.format("%s as %s", npair.render(), ff.asStr));
			} else {
				joiner.add(String.format("%s", npair.render()));
			}
		}
		return joiner;
	}

	private boolean isStructMatch(HLDField ff, StructFieldOpt structField2) {
		if (ff.structType == structField2.dtype) {
			if (ff.fieldName.equals(structField2.fieldName)) {
				return true;
			}
		}
		return false;
	}

	private SqlColumn addFuncs(SqlColumn npair, HLDField ff, HLDQuery hld) {
		Optional<QueryFnSpec> opt = hld.funcL.stream().filter(x ->x.isMatch(ff.structType, ff.fieldName)).findAny();
		if (! opt.isPresent()) {
			return npair;
		}
		
		QueryFnSpec qfn = opt.get();
		switch(qfn.getFnName()) {
		case "min":
		case "max":
		case "count":
			return addFunc(npair, qfn); 
			
		case "exists":
			return addFuncEx(npair, "count"); 
			
		//handled at select level (entire statement)
		case "distinct": 
		case "orderBy": 
		case "limit": 
		case "offset": 
		case "first": 
		case "last": 
		case "ith": 
			return npair; 
		default:
			break;
		}
		
		DeliaExceptionHelper.throwNotImplementedError("unknown fn: %s", qfn.getFnName());
		return null;
	}

	private SqlColumn addFunc(SqlColumn npair, QueryFnSpec qfn) {
		npair.name = String.format("%s(%s)", qfn.getFnName(), npair.render());
		npair.alias = null;
		return npair;
	}
	private SqlColumn addFuncEx(SqlColumn npair, String str) {
		npair.name = String.format("%s(%s)", str, npair.render());
		npair.alias = null;
		return npair;
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

	private void generateWhere(StrCreator sc, SqlStatement stm) {
		//TODO sc.o fails if string has % in it. fix this in many places in these genrrators!!
		sc.addStr(whereClause.render(stm));
	}
}
