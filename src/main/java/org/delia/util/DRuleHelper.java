package org.delia.util;

import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;


public class DRuleHelper {
	
	public static TypePair findMatchingPair(DStructType structType, String fieldName) {
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				if (fieldName.equals(pair.name)) {
					return pair;
				}
			}
		}
		return null;
	}
	public static TypePair findMatchingRelByType(DStructType dtype, DType targetType) {
		//TODO: later also use named relations
		for(TypePair pair: dtype.getAllFields()) {
			if (typesAreEqual(pair.type, targetType)) {
				return pair;
			}
		}
		return null;
	}
	
	private static boolean typesAreEqual(DType type1, DType type2) {
		String s1 = type1.getName();
		String s2 = type2.getName();
		return s1.equals(s2);
	}
	
	public static boolean isParentRelation(DStructType structType, TypePair pair) {
		//key goes in child only
		RelationInfo info = DRuleHelper.findMatchingRuleInfo(structType, pair);
		if (info != null && info.isParent) {
			return true;
		}
		return false;
	}
	public static RelationInfo findMatchingRuleInfo(DStructType structType, TypePair pair) {
		for(DRule rule: structType.getRawRules()) {
			if (rule instanceof RelationOneRule) {
				RelationOneRule rr = (RelationOneRule) rule;
				if (rr.relInfo.fieldName.equals(pair.name)) {
					return rr.relInfo;
				}
			} else if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
				if (rr.relInfo.fieldName.equals(pair.name)) {
					return rr.relInfo;
				}
			}
		}
		return null;
	}

	public static RelationOneRule findOneRule(String typeName, String fieldName, DTypeRegistry registry) {
		DStructType dtype = (DStructType) registry.getType(typeName);
		for(DRule rule: dtype.getRawRules()) {
			if (rule instanceof RelationOneRule) {
				if (rule.getSubject().equals(fieldName)) {
					return (RelationOneRule) rule;
				}
			}
		}
		return null;
	}
	public static RelationManyRule findManyRule(String typeName, String fieldName, DTypeRegistry registry) {
		DStructType dtype = (DStructType) registry.getType(typeName);
		for(DRule rule: dtype.getRawRules()) {
			if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
				if (rule.getSubject().equals(fieldName)) {
					return (RelationManyRule) rule;
				}
			}
		}
		return null;
	}
	
	public static boolean isOtherSideOne(DType otherSide, DStructType structType) {
		RelationInfo info = findOtherSideOne(otherSide, structType);
		return (info != null);
	}
	public static RelationInfo findOtherSideOne(DType otherSide, DStructType structType) {
		for(DRule rule: otherSide.getRawRules()) {
			if (rule instanceof RelationOneRule) {
				RelationOneRule rr = (RelationOneRule) rule;
				if (typesAreEqual(rr.relInfo.farType, structType)) {
					return rr.relInfo;
				}
			}
		}
		return null;
	}
	public static RelationInfo findOtherSideMany(DType otherSide, DStructType structType) {
		for(DRule rule: otherSide.getRawRules()) {
			if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
				if (typesAreEqual(rr.relInfo.farType, structType)) {
					return rr.relInfo;
				}
			}
		}
		return null;
	}
	public static RelationInfo findOtherSideOneOrMany(DType otherSide, DStructType structType) {
		RelationInfo farInfo = DRuleHelper.findOtherSideOne(otherSide, structType);
		if (farInfo == null) {
			farInfo = DRuleHelper.findOtherSideMany(otherSide, structType);
		}
		return farInfo;
	}
	public static RelationInfo findOtherSideOneOrManyForField(DType structType, String fieldName) {
		for(DRule rule: structType.getRawRules()) {
			if (rule instanceof RelationOneRule) {
				RelationOneRule rr = (RelationOneRule) rule;
				if (rr.relInfo.fieldName.equals(fieldName)) {
					return rr.relInfo;
				}
			} else if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
				if (rr.relInfo.fieldName.equals(fieldName)) {
					return rr.relInfo;
				}
			}
		}
		return null;
	}
	public static boolean isOtherSideMany(DType otherSide, TypePair otherRelPair) {
		for(DRule rule: otherSide.getRawRules()) {
			if (otherRelPair.name.equals(rule.getSubject())) {
				return rule instanceof RelationManyRule;
			}
		}
		return false;
	}
	public static boolean isManyToManyRelation(TypePair pair, DStructType dtype) {
		if (! dtype.isStructShape()) {
			return false;
		}
		//key goes in child only
		RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, pair);
		if (info != null && info.cardinality.equals(RelationCardinality.MANY_TO_MANY)) {
			return true;
		}
		return false;
	}
	public static RelationInfo findManyToManyRelation(TypePair pair, DStructType dtype) {
		if (! dtype.isStructShape()) {
			return null;
		}
		//key goes in child only
		RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, pair);
		if (info != null && info.cardinality.equals(RelationCardinality.MANY_TO_MANY)) {
			return info;
		}
		return null;
	}


}