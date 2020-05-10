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
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.fragment.AliasedFragment;
import org.delia.db.sql.fragment.FieldFragment;
import org.delia.db.sql.fragment.MiniSelectFragmentParser;
import org.delia.db.sql.fragment.OpFragment;
import org.delia.db.sql.fragment.SelectStatementFragment;
import org.delia.db.sql.fragment.SqlFragment;
import org.delia.db.sql.fragment.TableFragment;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.relation.RelationInfo;
import org.delia.runner.FilterEvaluator;
import org.delia.runner.VarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DeliaExceptionHelper;

public class HLSSQLGeneratorImpl extends ServiceBase implements HLSSQLGenerator {

//	private DTypeRegistry registry;
	private QueryTypeDetector queryTypeDetector;
	private QueryExp queryExp;
	private AliasAllocator aliasAlloc = new AliasAllocator();
	private SqlJoinHelper joinHelper;
	private AssocTblManager assocTblMgr;
	private MiniSelectFragmentParser miniSelectParser;
	private VarEvaluator varEvaluator;

	public HLSSQLGeneratorImpl(FactoryService factorySvc, AssocTblManager assocTblMgr, MiniSelectFragmentParser miniSelectParser, VarEvaluator varEvaluator) {
		super(factorySvc);
		this.joinHelper = new SqlJoinHelper(aliasAlloc, assocTblMgr);
		this.assocTblMgr = assocTblMgr;
		this.miniSelectParser = miniSelectParser;
		this.varEvaluator = varEvaluator;
	}

	@Override
	public String buildSQL(HLSQueryStatement hls) {
		this.queryExp = hls.queryExp;
		if (hls.hlspanL.size() == 1) {
			return processOneStatement(hls.getMainHLSSpan(), false);
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
				return sql;
			} else if (QueryType.PRIMARY_KEY.equals(queryType)){
				SUBElement subEl = new SUBElement();
				subEl.allFKs = true;
				hlspan1.subEl = subEl;
				hlspan1.doubleFlip = true;
				//1 - Address, 2 - Customer
				
				String sql = processOneStatement(hlspan1, false);
				
				SQLCreator sc = new SQLCreator();
				genWhere(sc, hlspan2);
				String s2 = sc.sql();
				String newAlias = "b";
				String fff = assocTblMgr.getAssocField(hlspan2.fromType);
				String s3 = String.format("%s.%s", newAlias, fff);
				
				String pkField = hlspan2.fromType.getPrimaryKey().getFieldName();
				String target = String.format("c.%s", pkField);
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
		joinHelper.genJoin(sc, hlspan);
	}
	private void addFKofJoins(HLSQuerySpan hlspan, List<String> fieldL) {
		joinHelper.addFKofJoins(hlspan, fieldL);
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
			sc.out("ORDER BY %s",ss);
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
		
		QuerySpec spec = new QuerySpec();
		spec.queryExp = queryExp;
		spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
		QueryDetails details = new QueryDetails();
		SelectStatementFragment selectFrag = miniSelectParser.parseSelect(spec, details);
		
		Map<String,String> aliasAdjustmentMap = new HashMap<>();
		for(String x: selectFrag.aliasMap.keySet()) {
			TableFragment tblfrag = selectFrag.aliasMap.get(x);
			if (tblfrag.structType != null) {
				String alias = this.aliasAlloc.findOrCreateFor(tblfrag.structType);
				aliasAdjustmentMap.put(tblfrag.alias,  alias);
			} else {
				String alias = this.aliasAlloc.buildTblAliasAssoc(tblfrag.name);
				aliasAdjustmentMap.put(tblfrag.alias,  alias);
			}
		}
		
		//now do adjustment
		for(SqlFragment z: selectFrag.whereL) {
			OpFragment op = (OpFragment) z;
			if (op.left != null) {
				AliasedFragment replacement = remapParentFieldIfNeeded(op.left, selectFrag);
				if (replacement != null) {
					op.left = replacement;
				}
				op.left.alias = aliasAdjustmentMap.get(op.left.alias);
			}
			if (op.right != null) {
				AliasedFragment replacement = remapParentFieldIfNeeded(op.left, selectFrag);
				if (replacement != null) {
					op.left = replacement;
				}
				op.right.alias = aliasAdjustmentMap.get(op.right.alias);
			}
		}
		
		String whereSql = miniSelectParser.renderSelect(selectFrag);
		
		if (!selectFrag.whereL.isEmpty()) {
			SqlStatement statement = selectFrag.statement;
			hlspan.paramL = statement.paramL;
			whereSql = StringUtils.substringAfter(whereSql, "WHERE ").trim();
			sc.out("WHERE %s", whereSql);
		}
	}
	
	private AliasedFragment remapParentFieldIfNeeded(AliasedFragment af, SelectStatementFragment selectFrag) {
		if (af instanceof FieldFragment) {
			FieldFragment ff = (FieldFragment) af;
			return xremapParentFieldIfNeeded(ff);
		} else {
			for (FieldFragment ff: selectFrag.hlsRemapList) {
				if (ff.alias.equals(af.alias) && ff.name.equals(af.name)) {
					return xremapParentFieldIfNeeded(ff);
				}
			}
		}
		return null;
	}
	private AliasedFragment xremapParentFieldIfNeeded(FieldFragment ff) {
		TypePair pair = new TypePair(ff.name, ff.fieldType);
		RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(ff.structType, pair);
		if (relinfo != null && relinfo.isParent) {
			//TODO need more foolproof way to find other side
			RelationInfo otherSide = DRuleHelper.findOtherSideOneOrMany(relinfo.farType, ff.structType);
			if (otherSide != null) {
				FieldFragment newff = new FieldFragment();
				newff.alias = aliasAlloc.findOrCreateFor(relinfo.farType);
				newff.name = relinfo.farType.getPrimaryKey().getFieldName();
				newff.fieldType = ff.structType;
				newff.structType = relinfo.farType;
				return newff;
			}
		}
		return null;
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