package org.delia.db.hls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QuerySpec;
import org.delia.db.TableExistenceService;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.fragment.MiniSelectFragmentParser;
import org.delia.runner.VarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;

public class HLSSQLGeneratorImpl extends ServiceBase implements HLSSQLGenerator {

//	private DTypeRegistry registry;
	private QueryTypeDetector queryTypeDetector;
	private QueryExp queryExp;
	private AliasAllocator aliasAlloc = new AliasAllocator();
	private SqlJoinHelper joinHelper;
	private AssocTblManager assocTblMgr;
	private WhereClauseHelper whereClauseHelper;
	public Map<String,String> asNameMap = new HashMap<>();
//	private MiniSelectFragmentParser miniSelectParser;
//	private TableExistenceService existSvc;

	public HLSSQLGeneratorImpl(FactoryService factorySvc, AssocTblManager assocTblMgr, MiniSelectFragmentParser miniSelectParser, VarEvaluator varEvaluator, TableExistenceService existSvc) {
		super(factorySvc);
		this.joinHelper = new SqlJoinHelper(aliasAlloc, assocTblMgr, asNameMap, miniSelectParser);
		this.assocTblMgr = assocTblMgr;
//		this.miniSelectParser = miniSelectParser;
		this.whereClauseHelper = new WhereClauseHelper(factorySvc, assocTblMgr, miniSelectParser, varEvaluator, asNameMap, aliasAlloc);
//		this.existSvc = existSvc;
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
				hlspan1.doubleFlip = true;
				//1 - Address, 2 - Customer
				
				String sql = processOneStatement(hlspan1, false);
				hls.details = hlspan1.details;
				return sql;
			} else if (QueryType.PRIMARY_KEY.equals(queryType)){
				SUBElement subEl = new SUBElement();
				subEl.allFKs = true;
				hlspan1.subEl = subEl;
				hlspan1.doubleFlip = true;
				//1 - Address, 2 - Customer
				
				String sql = processOneStatement(hlspan1, false);
				hls.details = hlspan1.details;
				
				SQLCreator sc = new SQLCreator();
				whereClauseHelper.genWhere(hlspan2, queryExp); 
				genWhere(sc, hlspan2);
				String s2 = sc.sql();
				
				String assocTblName = assocTblMgr.getTableFor(hlspan1.fromType, hlspan2.fromType);
				String newAlias = aliasAlloc.findOrCreateForAssoc(assocTblName);
				String fff = assocTblMgr.getAssocField(hlspan1.fromType, hlspan2.fromType);
				String s3 = String.format("%s.%s", newAlias, fff);
				
				String pkField = hlspan2.fromType.getPrimaryKey().getFieldName();
				String alias2 = aliasAlloc.findOrCreateFor(hlspan2.fromType);
				String target = String.format("%s.%s", alias2, pkField);
				s2 = s2.replace(target, s3);		
				return sql + " " + s2;
			}
		} 
		//need to handle this at a higher level
		DeliaExceptionHelper.throwError("notsupported", "Not supported by HLSSQLGenerator");
		return null; //not supported
	}
	
	@Override
	public String processOneStatement(HLSQuerySpan hlspan, boolean forceAllFields) {
		if (hlspan.fromType != null) {
			aliasAlloc.findOrCreateFor(hlspan.fromType);
		}
		this.whereClauseHelper.genWhere(hlspan, queryExp); //need this to genereate "as " in fields
		
		SQLCreator sc = new SQLCreator();
		//SELECT .. from .. ..join.. ..where.. ..order..
		sc.out("SELECT");
		genFields(sc, hlspan, forceAllFields);
		sc.out("FROM %s", buildTblAlias(hlspan.mtEl.structType));
		genJoin(sc, hlspan);
		genWhere(sc, hlspan);

		genOLO(sc, hlspan);

		return sc.sql();
	}


	private String buildTblAlias(DStructType structType) {
		return aliasAlloc.buildTblAlias(structType);
	}
//	private String buildAlias(DStructType pairType, TypePair pair) {
//		return aliasAlloc.buildAlias(pairType, pair);
//	}
	private String buildAlias(DStructType pairType, String fieldName) {
		return aliasAlloc.buildAlias(pairType, fieldName);
	}

	private void genJoin(SQLCreator sc, HLSQuerySpan hlspan) {
		hlspan.details = joinHelper.genJoin(sc, hlspan);
	}
	private void addFKofJoins(HLSQuerySpan hlspan, List<String> fieldL) {
		boolean addedOne = joinHelper.addFKofJoins(hlspan, fieldL);
		if (addedOne) {
			int n = fieldL.size();
			String fieldStr = fieldL.get(n - 1).trim();
			if (whereClauseHelper.asNameMap.containsKey(fieldStr)) {
				String asName = whereClauseHelper.asNameMap.get(fieldStr);
				fieldStr = String.format("%s as %s", fieldStr, asName);
				fieldL.remove(n - 1);
				fieldL.add(fieldStr);
			}
		}
	}
	private void addFullofJoins(HLSQuerySpan hlspan, List<String> fieldL) {
		joinHelper.addFullofJoins(hlspan, fieldL);
	}


	private void genOLO(SQLCreator sc, HLSQuerySpan hlspan) {
		boolean needLimit1 = hlspan.hasFunction("exists");

		if (hlspan.oloEl == null) {
			if (needLimit1) {
				sc.out("LIMIT 1");
			}
			return;
		}

		if (hlspan.oloEl.orderBy != null) {
			String ss = buildAlias(hlspan.fromType, hlspan.oloEl.orderBy);
			String asc = hlspan.oloEl.isAsc ? "" : " desc";
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
		List<String> fieldL = new ArrayList<>();

		if (hlspan.hasFunction("first")) {
			sc.out("TOP 1");
		}

		boolean isJustFieldName = false;
		if (hlspan.fEl != null) {
			String fieldName = hlspan.fEl.getFieldName();
			String aa = buildAlias(hlspan.fromType, fieldName);
			if (hlspan.hasFunction("count")) {
				String s = String.format("COUNT(%s)", aa);
				fieldL.add(s);
			} else if (hlspan.hasFunction("min")) {
				String s = String.format("MIN(%s)", aa);
				fieldL.add(s);
			} else if (hlspan.hasFunction("max")) {
				String s = String.format("MAX(%s)", aa);
				fieldL.add(s);
			} else if (hlspan.hasFunction("distinct")) {
				String s = String.format("DISTINCT(%s)", aa);
				fieldL.add(s);
			} else if (hlspan.hasFunction("exists")) {
				String s = String.format("COUNT(%s)", aa);
				fieldL.add(s);
			} else {
				fieldL.add(aa);
				isJustFieldName = true;
			}
		} else  {
			if (hlspan.hasFunction("count")) {
				String s = String.format("COUNT(*)");
				fieldL.add(s);
			} else if (hlspan.hasFunction("exists")) {
				String s = String.format("COUNT(*)");
				fieldL.add(s);
			}
		}

		if (forceAllFields) {
			addStructFields(hlspan.fromType, fieldL);
		}
		
		boolean needJoin = hlspan.subEl != null;
		if (needJoin && fieldL.isEmpty()) {
			addStructFields(hlspan.fromType, fieldL);
			addFKofJoins(hlspan, fieldL);
			addFullofJoins(hlspan, fieldL);
		} else if (isJustFieldName) {
			addFKofJoins(hlspan, fieldL);
		}

		if (fieldL.isEmpty()) {
			fieldL.add("*");
		}

		StringJoiner joiner = new StringJoiner(",");
		for(String s: fieldL) {
			joiner.add(s);
		}
		sc.out(joiner.toString());
	}

	private void addStructFields(DStructType fromType, List<String> fieldL) {
		joinHelper.addStructFields(fromType, fieldL);
	}
	@Override
	public void setRegistry(DTypeRegistry registry) {
//		this.registry = registry;
		this.queryTypeDetector = new QueryTypeDetector(factorySvc, registry);
	}
}