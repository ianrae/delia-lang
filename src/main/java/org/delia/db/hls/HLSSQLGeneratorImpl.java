package org.delia.db.hls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QuerySpec;
import org.delia.db.hls.join.SqlJoinHelper;
import org.delia.db.hls.join.SqlJoinHelperImpl;
import org.delia.db.hls.join.SqlJoinTreeHelper;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.fragment.MiniSelectFragmentParser;
import org.delia.relation.RelationInfo;
import org.delia.runner.VarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.PrimaryKey;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DeliaExceptionHelper;

public class HLSSQLGeneratorImpl extends ServiceBase implements HLSSQLGenerator {
	public static boolean useJoinTreeFlag = false;

	private QueryTypeDetector queryTypeDetector;
	private QueryExp queryExp;
	private SqlJoinHelper joinHelper;
	private WhereClauseHelper whereClauseHelper;
	public Map<String,String> asNameMap = new HashMap<>();
	private AliasManager aliasManager;
	private DatIdMap datIdMap;

	public HLSSQLGeneratorImpl(FactoryService factorySvc, MiniSelectFragmentParser miniSelectParser, VarEvaluator varEvaluator, AliasManager aliasManager, DatIdMap datIdMap) {
		super(factorySvc);
		
		if (useJoinTreeFlag) {
			this.joinHelper = new SqlJoinTreeHelper(aliasManager, datIdMap, asNameMap, miniSelectParser);
		} else {
			this.joinHelper = new SqlJoinHelperImpl(aliasManager, datIdMap, asNameMap, miniSelectParser);
			
		}
		this.whereClauseHelper = new WhereClauseHelper(factorySvc, miniSelectParser, varEvaluator, asNameMap, aliasManager, datIdMap);
		this.aliasManager = aliasManager;
		this.datIdMap = datIdMap;
	}

	@Override
	public String buildSQL(HLSQueryStatement hls) {
		this.queryExp = hls.queryExp;
		
		if (hls.hlspanL.size() == 1) {
			String sql = processOneStatement(hls.getMainHLSSpan(), false);
			hls.details = hls.getMainHLSSpan().details;
			return sql;
		} else if (hls.hlspanL.size() == 2) {
			
			HLSQuerySpan hlspan1 = hls.hlspanL.get(1); //Address
			HLSQuerySpan hlspan2 = hls.hlspanL.get(0); //Customer
			
			QueryType queryType = detectQueryType(hlspan2);
			
			if (QueryType.ALL_ROWS.equals(queryType)) {
				SUBElement subEl = new SUBElement();
				subEl.allFKs = true;
				hlspan1.subEl = subEl;
				//1 - Address, 2 - Customer
				
				String sql = processOneStatement(hlspan1, false);
				hls.details = hlspan1.details;
				return sql;
			} else if (QueryType.PRIMARY_KEY.equals(queryType)){
				SUBElement subEl = new SUBElement();
				subEl.allFKs = true;
				hlspan1.subEl = subEl;
				//1 - Address, 2 - Customer
				
				String sql = processOneStatement(hlspan1, false);
				hls.details = hlspan1.details;
				
				SQLCreator sc = new SQLCreator();
				whereClauseHelper.genWhere(hlspan2); 
				genWhere(sc, hlspan2);
				String s2 = sc.sql();
				
				TypePair relPair = hlspan1.rEl.rfieldPair;
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(hlspan2.fromType, relPair);
				switch(relinfo.cardinality) {
				case ONE_TO_ONE:
				case ONE_TO_MANY:
				{
					if (isQueryPKOnly(hlspan1)) {
//						String alias = aliasAlloc.findOrCreateFor(hlspan1.fromType);
						String alias = aliasManager.getMainTableAlias(hlspan1.fromType).alias;
						RelationInfo relinfo1 = relinfo.otherSide; 
						sql = String.format("%s WHERE %s.%s=?", sql, alias, relinfo1.fieldName);
						return sql;
					} else {
						RelationInfo otherSide = relinfo.otherSide; 
						String pkField = hlspan2.fromType.getPrimaryKey().getFieldName();
						s2 = StringUtils.substringAfter(s2, "WHERE ");
//						String alias1 = aliasAlloc.findOrCreateFor(relinfo.farType);
//						String alias2 = aliasAlloc.findOrCreateFor(hlspan2.fromType);
						AliasInfo alias1 = aliasManager.getFieldAlias(relinfo.nearType, relinfo.fieldName);
						AliasInfo alias2 = aliasManager.getMainTableAlias(hlspan2.fromType);
						
						sql = String.format("%s AND %s WHERE %s.%s=%s.%s", sql, s2, alias1, otherSide.fieldName, alias2.alias, pkField);
						
						return sql;
					}
				}
				case MANY_TO_MANY:
				{
					String assocTblName = datIdMap.getAssocTblName(relinfo.getDatId()); 
//					String newAlias = aliasAlloc.findOrCreateForAssoc(assocTblName);
					String newAlias = aliasManager.getAssocAlias(relinfo.nearType, relinfo.fieldName, assocTblName).alias;
					String fff = datIdMap.getAssocFieldFor(relinfo); //hlspan2.fromType);
					String s3 = String.format("%s.%s", newAlias, fff);
					
					String pkField = hlspan2.fromType.getPrimaryKey().getFieldName();
//					String alias2 = aliasAlloc.findOrCreateFor(hlspan2.fromType);
					String alias2 = aliasManager.getMainTableAlias(hlspan2.fromType).alias;
					String target = String.format("%s.%s", alias2, pkField);
					s2 = s2.replace(target, s3);		
					return sql + " " + s2;
				}
				}
			}
		} 
		//need to handle this at a higher level
		DeliaExceptionHelper.throwError("notsupported", "Not supported by HLSSQLGenerator");
		return null; //not supported
	}
	
	private boolean isQueryPKOnly(HLSQuerySpan hlspan1) {
		if (hlspan1.fEl == null) {
			return false;
		}
		
		PrimaryKey pk = hlspan1.fromType.getPrimaryKey();
		if (pk != null && pk.getFieldName().equals(hlspan1.fEl.fieldPair.name)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String processOneStatement(HLSQuerySpan hlspan, boolean forceAllFields) {
//		if (hlspan.fromType != null) {
//			aliasAlloc.findOrCreateFor(hlspan.fromType);
//		}
		this.whereClauseHelper.genWhere(hlspan); //need this to genereate "as " in fields
		
		SQLCreator sc = new SQLCreator();
		//SELECT .. from .. ..join.. ..where.. ..order..
		sc.out("SELECT");
		genFields(sc, hlspan, forceAllFields);
		
		AliasInfo aliasInfo = aliasManager.getMainTableAlias(hlspan.mtEl.structType);
		sc.out("FROM %s", aliasManager.buildTblAlias(aliasInfo));
		genJoin(sc, hlspan);
		genWhere(sc, hlspan);

		genOLO(sc, hlspan);

		return sc.sql();
	}

//	private String buildAlias(DStructType pairType, String fieldName) {
////		return aliasAlloc.buildAlias(pairType, fieldName);
//		return aliasManager.getFieldAlias(pairType, fieldName).alias;
//	}
	private String buildMainAlias(DStructType fromType, String fieldName) {
		AliasInfo info = aliasManager.getMainTableAlias(fromType);
		return aliasManager.buildFieldAlias(info, fieldName);
	}

	private void genJoin(SQLCreator sc, HLSQuerySpan hlspan) {
		hlspan.details = joinHelper.genJoin(sc, hlspan);
	}
	private void addFKofJoins(HLSQuerySpan hlspan, List<RenderedField> fieldL) {
		int numAdded = joinHelper.addFKofJoins(hlspan, fieldL);
		if (numAdded > 0) {
			for(int k = 0; k < numAdded; k++) {
				int n = fieldL.size();
				RenderedField rf = fieldL.get(n - (k + 1));
				String fieldStr = rf.field.trim();
				if (whereClauseHelper.asNameMap.containsKey(fieldStr)) {
					String asName = whereClauseHelper.asNameMap.get(fieldStr);
					fieldStr = String.format("%s as %s", fieldStr, asName);
//				fieldL.remove(n - 1);
					rf.field = fieldStr;
//				fieldL.add(fieldStr);
				}
			}
		}
	}
//	private void addFullofJoins(HLSQuerySpan hlspan, List<RenderedField> fieldL) {
//		joinHelper.addFullofJoins(hlspan, fieldL);
//	}


	protected void genOLO(SQLCreator sc, HLSQuerySpan hlspan) {
		boolean needLimit1 = oloNeedsLimit(hlspan); 
		boolean hasLast = hlspan.hasFunction("last");
		boolean hasIth = hlspan.hasFunction("ith");

		if (hlspan.oloEl == null) {
			if (hasLast) {
				//implicitly add sort by pk (if there is one)
				PrimaryKey pk = hlspan.fromType.getPrimaryKey();
				if (pk != null) {
					String ss = buildMainAlias(hlspan.fromType, pk.getFieldName());
					sc.out("ORDER BY %s desc",ss);
				}
			}
			if (hasIth) {
				GElement gel = hlspan.findFunction("ith");
				//implicitly add sort by pk (if there is one)
				PrimaryKey pk = hlspan.fromType.getPrimaryKey();
				if (pk != null) {
					String ss = buildMainAlias(hlspan.fromType, pk.getFieldName());
					Integer iOffset = gel.getIntArg(0);
					sc.out("ORDER BY %s LIMIT 1 OFFSET %s",ss, iOffset.toString());
				}
			}
			if (needLimit1) {
				sc.out("LIMIT 1");
			}
			return;
		}

		if (hlspan.oloEl.orderBy != null) {
			boolean isAsc = hlspan.oloEl.isAsc;
			if (hlspan.hasFunction("last")) { //we apply desc sorting
				isAsc = false;
			}
			String asc = isAsc ? "" : " desc";
			//FUTURE later support order by doing implicit fetch. orderBy(addr.city)
			AliasInfo aliasInfo = aliasManager.getMainTableAlias(hlspan.fromType);
			String ss = aliasManager.buildFieldAlias(aliasInfo, hlspan.oloEl.orderBy);
			sc.out("ORDER BY %s%s",ss, asc);
		}

		if (hlspan.oloEl.limit != null) {
			sc.out("LIMIT %s", hlspan.oloEl.limit.toString());
		} else if (needLimit1) {
			sc.out("LIMIT 1");
		}

		if (hlspan.oloEl.offset != null) {
			sc.out("OFFSET %s", hlspan.oloEl.offset.toString());
		}
	}

	protected boolean oloNeedsLimit(HLSQuerySpan hlspan) {
		return hlspan.hasFunction("exists");
	}

	private void genWhere(SQLCreator sc, HLSQuerySpan hlspan) {
		if (hlspan.filEl ==  null) {
			return;
		}
	
		if (StringUtils.isNotEmpty(hlspan.finalWhereSql)) {
			sc.out(hlspan.finalWhereSql);
		}
	}
	
	private QueryType detectQueryType(HLSQuerySpan hlspan) {
		QuerySpec spec = new QuerySpec();
		spec.queryExp = queryExp;
		QueryType queryType = queryTypeDetector.detectQueryType(spec);
		return queryType;
	}
	

	private void genFields(SQLCreator sc, HLSQuerySpan hlspan, boolean forceAllFields) {
		List<RenderedField> fieldL = new ArrayList<>();

		if (hlspan.hasFunction("first")) {
			doFirst(sc, hlspan);
		}
		if (hlspan.hasFunction("last")) { //we apply desc sorting
			doLast(sc, hlspan);
		}

		boolean isJustFieldName = false;
		if (hlspan.fEl != null) {
			String fieldName = hlspan.fEl.getFieldName();
			AliasInfo aliasInfo = aliasManager.getMainTableAlias(hlspan.fromType);
			String aa = aliasManager.buildFieldAlias(aliasInfo, fieldName);
			if (hlspan.hasFunction("count")) {
				String s = String.format("COUNT(%s)", aa);
				addField(fieldL, s);
			} else if (hlspan.hasFunction("min")) {
				String s = String.format("MIN(%s)", aa);
				addField(fieldL, s);
			} else if (hlspan.hasFunction("max")) {
				String s = String.format("MAX(%s)", aa);
				addField(fieldL, s);
			} else if (hlspan.hasFunction("distinct")) {
				String s = String.format("DISTINCT(%s)", aa);
				addField(fieldL, s);
			} else if (hlspan.hasFunction("exists")) {
				String s = String.format("COUNT(%s)", aa);
				addField(fieldL, s);
			} else {
				addField(fieldL, hlspan.fromType, hlspan.fEl.fieldPair, aa);
				isJustFieldName = true;
			}
		} else  {
			if (hlspan.hasFunction("count")) {
				String s = String.format("COUNT(*)");
				addField(fieldL, s);
			} else if (hlspan.hasFunction("exists")) {
				String s = String.format("COUNT(*)");
				addField(fieldL, s);
			}
		}

		//not needed i think
//		if (forceAllFields) {
//			addStructFields(hlspan.fromType, fieldL);
//		}
		
		boolean needJoin = joinHelper.needJoin(hlspan);
		if (needJoin && fieldL.isEmpty()) {
			joinHelper.addStructFields(hlspan.fromType, fieldL);
			if (joinHelper.supportsAddAllJoins()) {
				joinHelper.addAllJoins(hlspan, fieldL);
			} else {
				addFKofJoins(hlspan, fieldL);
				joinHelper.addFullofJoins(hlspan, fieldL);
//				addRelFieldJoin(hlspan);
			}
		} else if (isJustFieldName) {
			addFKofJoins(hlspan, fieldL);
		}

		if (fieldL.isEmpty()) {
			RenderedField rf = new RenderedField();
			rf.field = "*";
			fieldL.add(rf);
		}

		StringJoiner joiner = new StringJoiner(",");
		for(RenderedField rf: fieldL) {
			joiner.add(rf.field);
		}
		hlspan.renderedFieldL = fieldL;
		sc.out(joiner.toString());
	}

	protected void doFirst(SQLCreator sc, HLSQuerySpan hlspan) {
		sc.out("TOP 1");
	}
	protected void doLast(SQLCreator sc, HLSQuerySpan hlspan) {
		sc.out("TOP 1");
	}
	

	private void addField(List<RenderedField> fieldL, DStructType structType, TypePair pair, String s) {
		RenderedField rf = new RenderedField();
		rf.field = s;
		rf.pair = pair;
		rf.structType = structType;
		fieldL.add(rf);
	}


	private void addField(List<RenderedField> fieldL, String s) {
		RenderedField rf = new RenderedField();
		rf.field = s;
		fieldL.add(rf);
	}

//	private void addStructFields(DStructType fromType, List<RenderedField> fieldL) {
//		joinHelper.addStructFields(fromType, fieldL);
//	}
	@Override
	public void setRegistry(DTypeRegistry registry) {
		this.queryTypeDetector = new QueryTypeDetector(factorySvc, registry);
	}
}