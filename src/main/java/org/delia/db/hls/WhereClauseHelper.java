package org.delia.db.hls;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
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
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;

public class WhereClauseHelper extends ServiceBase {

	private AliasAllocator aliasAlloc;
	private MiniSelectFragmentParser miniSelectParser;
	private VarEvaluator varEvaluator;
	public Map<String,String> asNameMap;
	
	public WhereClauseHelper(FactoryService factorySvc, AssocTblManager assocTblMgr, MiniSelectFragmentParser miniSelectParser, VarEvaluator varEvaluator, 
				Map<String, String> asNameMap, AliasAllocator aliasAlloc) {
		super(factorySvc);
		this.miniSelectParser = miniSelectParser;
		this.varEvaluator = varEvaluator;
		this.asNameMap = asNameMap;
		this.aliasAlloc = aliasAlloc;
	}

	public void genWhere(HLSQuerySpan hlspan) {
		if (hlspan.filEl ==  null) {
			return;
		}
		QuerySpec spec = new QuerySpec();
		spec.queryExp = hlspan.filEl.queryExp;
		spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
		QueryDetails details = new QueryDetails();
		SelectStatementFragment selectFrag = miniSelectParser.parseSelect(spec, details);
		
		//so selectFrag.whereL has the filter expression
		//if it contains any parent relation fields (Customer.addr)
		//we need to change them to other side's pk.
		//Also need to merge fragment parsers' table aliases with ours.
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
				if (!remapParentFieldIfNeeded(op.left, selectFrag, aliasAdjustmentMap)) {
					op.left.alias = aliasAdjustmentMap.get(op.left.alias);
				}
			}
			if (op.right != null) {
				if (!remapParentFieldIfNeeded(op.right, selectFrag, aliasAdjustmentMap)) {
					op.right.alias = aliasAdjustmentMap.get(op.right.alias);
				}
			}
		}
		
		String whereSql = miniSelectParser.renderSelect(selectFrag);
		
		hlspan.finalWhereSql = "";
		if (!selectFrag.whereL.isEmpty()) {
			SqlStatement statement = selectFrag.statement;
			hlspan.paramL = statement.paramL;
			whereSql = StringUtils.substringAfter(whereSql, "WHERE ").trim();
			hlspan.finalWhereSql = String.format("WHERE %s", whereSql);
		}
	}
	
	private boolean remapParentFieldIfNeeded(AliasedFragment af, SelectStatementFragment selectFrag, Map<String, String> aliasAdjustmentMap) {
		if (af instanceof FieldFragment) {
			FieldFragment ff = (FieldFragment) af;
			return remapParentFieldIfNeeded(ff, af, aliasAdjustmentMap);
		} else {
			for (FieldFragment ff: selectFrag.hlsRemapList) {
				if (ff.alias.equals(af.alias) && ff.name.equals(af.name)) {
					return remapParentFieldIfNeeded(ff, af, aliasAdjustmentMap);
				}
			}
		}
		return false;
	}
	private boolean remapParentFieldIfNeeded(FieldFragment ff, AliasedFragment af, Map<String,String> aliasAdjustmentMap) {
		TypePair pair = new TypePair(ff.name, ff.fieldType);
		RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(ff.structType, pair);
		if (relinfo != null && relinfo.isParent) {
			//TODO need more foolproof way to find other side
			RelationInfo otherSide = relinfo.otherSide; //DRuleHelper.findOtherSideOneOrMany(relinfo.farType, ff.structType);
			if (otherSide != null) {
				af.name = relinfo.farType.getPrimaryKey().getFieldName();
				AliasInstance aliasInst = aliasAlloc.findOrCreateAliasInstance(relinfo.farType, pair.name);
				af.alias = aliasInst.alias; //aliasAdjustmentMap.get(aliasInst.alias);
				
				String key = String.format("%s.%s", af.alias, af.name);
				asNameMap.put(key, relinfo.fieldName);
				return true;
			}
		}
		return false;
	}
}