package org.delia.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.delia.relation.RelationInfo;
import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.rule.rules.RelationRuleBase;
//import org.delia.rule.rules.SizeofRule;
import org.delia.rule.rules.SizeofRule;
import org.delia.type.*;


public class DRuleHelper {

	public static TypePair findMatchingPair(DStructType structType, String fieldName) {
		for(TypePair pair: structType.getAllFields()) {
			if (fieldName.equals(pair.name)) {
				return pair;
			}
		}
		return null;
	}
	public static TypePair findMatchingStructPair(DStructType structType, String fieldName) {
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				if (fieldName.equals(pair.name)) {
					return pair;
				}
			}
		}
		return null;
	}
	public static List<TypePair> xfindAllMatchingRelByType(DStructType dtype, DType targetType) {
		List<TypePair> list = new ArrayList<>();
		for(TypePair pair: dtype.getAllFields()) {
			if (typesAreEqual(pair.type, targetType)) {
				list.add(pair);
			}
		}
		return list;
	}

	public static boolean typesAreEqual(DType type1, DType type2) {
		DTypeName s1 = type1.getTypeName();
		DTypeName s2 = type2.getTypeName();
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
	public static RelationInfo findMatchingRuleInfo(DStructType structType, String fieldName) {
		TypePair pair = new TypePair(fieldName, null);
		return findMatchingRuleInfo(structType, pair);
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
	public static RelationInfo findMatchingByName(RelationRuleBase rrTarget, DStructType farType) {
		if (rrTarget.getRelationName() == null) {
			return null;
		}
		String name = rrTarget.getRelationName();

		for(DRule rule: farType.getRawRules()) {
			if (rule instanceof RelationOneRule) {
				RelationOneRule rr = (RelationOneRule) rule;
				if (name.equals(rr.getRelationName())) {
					return rr.relInfo;
				}
			} else if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
				if (name.equals(rr.getRelationName())) {
					return rr.relInfo;
				}
			}
		}
		return null;
	}
	//needed due to self-join
	public static List<RelationInfo> findAllMatchingByName(RelationRuleBase rrTarget, DStructType farType) {
		if (rrTarget.getRelationName() == null) {
			return Collections.emptyList();
		}
		String name = rrTarget.getRelationName();
		List<RelationInfo> resultL = new ArrayList<>();

		for(DRule rule: farType.getRawRules()) {
			if (rule instanceof RelationOneRule) {
				RelationOneRule rr = (RelationOneRule) rule;
				if (name.equals(rr.getRelationName())) {
					resultL.add(rr.relInfo);
				}
			} else if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
				if (name.equals(rr.getRelationName())) {
					resultL.add(rr.relInfo);
				}
			}
		}
		return resultL;
	}

	public static RelationOneRule findOneRule(DStructType dtype, String fieldName) {
		for(DRule rule: dtype.getRawRules()) {
			if (rule instanceof RelationOneRule) {
				if (rule.getSubject().equals(fieldName)) {
					return (RelationOneRule) rule;
				}
			}
		}
		return null;
	}
	public static RelationManyRule findManyRule(DStructType dtype, String fieldName) {
		for(DRule rule: dtype.getRawRules()) {
			if (rule instanceof RelationManyRule) {
				if (rule.getSubject().equals(fieldName)) {
					return (RelationManyRule) rule;
				}
			}
		}
		return null;
	}

	public static RelationInfo findOtherSideOne(RelationInfo relinfo) {
		for(DRule rule: relinfo.farType.getRawRules()) {
			if (rule instanceof RelationOneRule) {
				RelationOneRule rr = (RelationOneRule) rule;
				if (rr.relInfo.otherSide == relinfo) {
					return rr.relInfo;
				}
			}
		}
		return null;
	}
	public static RelationInfo findOtherSideMany(RelationInfo relinfo) {
		for(DRule rule: relinfo.farType.getRawRules()) {
			if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
				if (rr.relInfo.otherSide == relinfo) {
					return rr.relInfo;
				}
			}
		}
		return null;
	}

	public static RelationInfo findRelinfoOneOrManyForField(DType structType, String fieldName) {
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
		if (info != null && info.isManyToMany()) {
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
		if (info != null && info.isManyToMany()) {
			return info;
		}
		return null;
	}
//	public static int getSizeofField(DStructType dtype, String fieldName) {
//		for(DRule rule: dtype.getRawRules()) {
//			if (rule instanceof SizeofRule) {
//				SizeofRule szrule = (SizeofRule) rule;
//				if (szrule.getSubject().equals(fieldName)) {
//					return szrule.getSizeofAmount();
//				}
//			}
//		}
//		return 0;
//	}

	public static DRule findSizeofRule(DType dtype, List<DRule> structRules, String fieldName) {
		List<DRule> possibleRules = dtype.getRawRules().stream().filter(x -> x instanceof SizeofRule).collect(Collectors.toList());
		possibleRules.addAll(structRules.stream().filter(x -> x instanceof SizeofRule).collect(Collectors.toList()));

		//TODO: what if > 1 possibleRules. is this an error or does one have priority?
		for (DRule rule : possibleRules) {
			String subject = rule.getSubject();
			if (subject == null) {
				return rule;
			} else if (subject.equals(fieldName)) {
				return rule;
			}
		}
		return null;
	}

	public static int getSizeofAmount(DRule rule, int defaultVal) {
		int sizeof = rule instanceof SizeofRule ? ((SizeofRule)rule).getSizeofAmount(): defaultVal;
		return sizeof;
	}
}