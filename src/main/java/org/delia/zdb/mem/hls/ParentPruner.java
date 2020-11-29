package org.delia.zdb.mem.hls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.delia.relation.RelationInfo;
import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.DValueImpl;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

public class ParentPruner {

	private DTypeRegistry registry;

	public ParentPruner(DTypeRegistry registry) {
		this.registry = registry;
	}

	public List<DValue> removeParentSideRelations(List<DValue> dvalList) {
		List<DValue> list = new ArrayList<>();
		for(DValue dval: dvalList) {
			dval = removeParentSideRelationsOne(dval);
			list.add(dval);
		}

		return list;
	}
	public DValue removeParentSideRelationsOne(DValue dval) {
		if (! dval.getType().isStructShape()) {
			return dval;
		}
		DStructType structType = (DStructType) dval.getType();
		List<String> doomedL =  new ArrayList<>();
		for(TypePair pair: structType.getAllFields()) {
			RelationOneRule oneRule = DRuleHelper.findOneRule(structType, pair.name);
			if (oneRule != null) {
				if (oneRule.relInfo != null && oneRule.relInfo.isParent) {
					doomedL.add(pair.name);
				}
			} else {
				RelationManyRule manyRule = DRuleHelper.findManyRule(structType, pair.name);
				if (manyRule != null) {
					//MM have no parent so don't check isParent flag
//					if (manyRule.relInfo != null && manyRule.relInfo.isParent) {
					if (manyRule.relInfo != null) {
						doomedL.add(pair.name);
					}
				}
			}
		}

		//clone without doomed fields
		if (doomedL.isEmpty()) {
			return dval;
		}

		Map<String,DValue> cloneMap = new TreeMap<>(dval.asMap());
		for(String doomed: doomedL) {
			cloneMap.remove(doomed);
		}

		DValueImpl clone = new DValueImpl(structType, cloneMap);
		return clone;
	}

	
	public void removeFetchedItems(DValue dval, String typeName, String fieldName) {
		DStructType targetStructType = (DStructType) registry.getType(typeName);
		if (targetStructType == null) {
			return;
		}
		
		for(DRule rule: dval.getType().getRawRules()) {
			if (rule instanceof RelationOneRule) {
				RelationOneRule rr = (RelationOneRule) rule;
				removeFromRelationFetchedItems(dval, rr.relInfo, targetStructType, fieldName);
			} else if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
				removeFromRelationFetchedItems(dval, rr.relInfo, targetStructType, fieldName);
			}
		}
	}
	
	private void removeFromRelationFetchedItems(DValue dval, RelationInfo relInfo, DStructType structType, String fieldName) {
		if (DValueHelper.typesAreSame(relInfo.farType, structType)) {
			DValue inner = dval.asStruct().getField(relInfo.fieldName);
			if (inner != null) {
				DRelation drel = inner.asRelation();
				if (drel.haveFetched()) {
					for(DValue x: drel.getFetchedItems()) {
						removeFieldFromSingleDVal(x, fieldName, structType);
					}
				}
			}
		}
	}

	public void removeFieldFromSingleDVal(DValue dval, String fieldName, DStructType structType) {
		for(TypePair pair: structType.getAllFields()) {
			if (pair.name.equals(fieldName)) {
				dval.asMap().remove(pair.name);
			}
		}
		DValueImpl dvalimpl = (DValueImpl) dval;
		dvalimpl.forceType(structType);
	}
}