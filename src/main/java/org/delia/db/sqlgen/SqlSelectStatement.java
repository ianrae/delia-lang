package org.delia.db.sqlgen;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.newhls.HLDField;
import org.delia.db.newhls.HLDQuery;
import org.delia.db.newhls.JoinElement;
import org.delia.db.newhls.QueryFnSpec;
import org.delia.db.newhls.SqlColumn;
import org.delia.db.newhls.SqlColumnBuilder;
import org.delia.db.newhls.SqlParamGenerator;
import org.delia.db.newhls.cud.TypeOrTable;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

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

		StringJoiner joiner = generateFields(hld);
		sc.o(joiner.toString());

		sc.o(" FROM %s as %s", hld.fromType.getName(), hld.fromAlias);

		generateJoins(sc, hld, stm, paramGen);
		generateWhere(sc, stm);
		generateOrderBy(sc, hld);
		stm.sql = sc.toString();
		return stm;
	}


	////==

	//		public String generateSqlWhere(HLDQuery hld, SqlStatement stm) {
	//			return sqlWhereGen.generateSqlWhere(hld, stm);
	//		}
	//		
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
			
			if (fnspec.filterFn.argL.size() > 1) {
				String asc = fnspec.filterFn.argL.get(1).asString();
				sc.o(" %s", asc);  
			}
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
		Optional<QueryFnSpec> optCountFn = hld.funcL.stream().filter(x -> x.filterFn.fnName.equals("count")).findAny();
		if (optCountFn.isPresent()) {
			joiner.add("count(*)");
			return joiner;
		}
		
		if (!hld.resultType.isStructShape()) {
			//TODO: max(t0.id)
		}

		for(HLDField ff: hld.fieldL) {
			SqlColumn npair = mapFieldIfNeeded(ff);
			if (ff.asStr != null) {
				joiner.add(String.format("%s as %s", npair.toString(), ff.asStr));
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

	private void generateWhere(StrCreator sc, SqlStatement stm) {
		//TODO sc.o fails if string has % in it. fix this in many places in these genrrators!!
		sc.addStr(whereClause.render(stm));
	}
}
