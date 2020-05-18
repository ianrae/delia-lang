package org.delia.rule.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.rule.DRule;
import org.delia.sort.topo.DeliaTypeSorter;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.StringUtil;

/**
 * When we build rules we don't yet have all types in registry.
 * So we'll do a post-processing step here.
 * -for one-to-many we want to set parent flag on the one side implicitly
 * 
 * @author Ian Rae
 *
 */
public class RulePostProcessor extends ServiceBase {
	
	public RulePostProcessor(FactoryService factorySvc) {
		super(factorySvc);
	}

	public void process(DTypeRegistry registry, List<DeliaError> allErrors) {
		List<DStructType> structsL = buildListAllStructs(registry);		
		buildRelInfos(structsL);
		step1HookupNamedRelations(structsL, allErrors);
		step2HookupNamedOtherSide(structsL, allErrors);
		step3HookupUnNamedRelations(structsL, allErrors);
		step2HookupNamedOtherSide(structsL, allErrors);
		setParentFlagsIfNeeded(registry);
		
//		setOtherSide(registry, allErrors);
//		checkForOtherSideDuplicates(registry, allErrors);
		
		//then validate types can be put into a correct dependency order (i.e. no cycles)
		DeliaTypeSorter typeSorter = new DeliaTypeSorter();
		try {
			List<String> orderL = typeSorter.topoSort(registry);
			log.log("types: %s", StringUtil.flatten(orderL));
		} catch (IllegalArgumentException e) {
			DeliaError err = new DeliaError("type-dependency-cycle", "did you forget a 'parent' modifier?");
			allErrors.add(err);
			return;
		}
	}
	
	private List<DStructType> buildListAllStructs(DTypeRegistry registry) {
		List<DStructType> list = new ArrayList<>();
		for(String typeName: registry.getAll()) {
			DType dtype = registry.getType(typeName);
			if (! dtype.isStructShape()) {
				continue;
			}
			DStructType structType = (DStructType) dtype;
			list.add(structType);
		}
		return list;
	}
	

	private void buildRelInfos(List<DStructType> structsL) {
		for(DStructType structType: structsL) {
			for(TypePair pair: structType.getAllFields()) {
				if (pair.type.isStructShape()) {
					
					for(DRule rule: structType.getRawRules()) {
						if (rule instanceof RelationOneRule) {
							RelationOneRule rr = (RelationOneRule) rule;
							RelationInfo info = new RelationInfo();
							rr.relInfo = info;
							TypePair farSide = DRuleHelper.findMatchingRelByType((DStructType)pair.type, structType);
							boolean b = farSide == null ? false : isOtherSideMany(pair.type, farSide);
							info.cardinality = b ? RelationCardinality.ONE_TO_MANY : RelationCardinality.ONE_TO_ONE;
							info.farType = (DStructType) pair.type;
							info.fieldName = rule.getSubject();
							info.isOneWay = (farSide == null);
							info.isParent = rr.isParent();
							info.nearType = structType;
							info.relationName = rr.getRelationName();
						} else if (rule instanceof RelationManyRule) {
							RelationManyRule rr = (RelationManyRule) rule;
							RelationInfo info = new RelationInfo();
							rr.relInfo = info;
							TypePair farSide = rr.findMatchingRel((DStructType)pair.type, structType);
							boolean b = isOtherSideMany(pair.type, farSide);
							info.cardinality = b ? RelationCardinality.MANY_TO_MANY : RelationCardinality.ONE_TO_MANY;
							info.farType = (DStructType) pair.type;
							info.fieldName = rule.getSubject();
							info.isOneWay = false;
							info.isParent = false; //will set after this
							info.nearType = structType;
							info.relationName = rr.getRelationName();
						}
					}
				}
			}
		}
	}
	
	private void step1HookupNamedRelations(List<DStructType> structsL, List<DeliaError> allErrors) {
		for(DStructType structType: structsL) {
			for(DRule rule: structType.getRawRules()) {
				if (rule instanceof RelationOneRule) {
					RelationOneRule rr = (RelationOneRule) rule;
					RelationInfo info = rr.relInfo;
					if (rr.nameIsExplicit) {
						if (info.otherSide == null) {
							info.otherSide = findOtherSideNamed(rr, rr.getRelationName(), info.farType, info.nearType, allErrors);
							logIfSet("step1", rr.getRelationName(), info);
						}
					}
				} else if (rule instanceof RelationManyRule) {
					RelationManyRule rr = (RelationManyRule) rule;
					RelationInfo info = rr.relInfo;
					if (rr.nameIsExplicit) {
						if (info.otherSide == null) {
							info.otherSide = findOtherSideNamed(rr, rr.getRelationName(), info.farType, info.nearType, allErrors);
							logIfSet("step1", rr.getRelationName(), info);
						}
					}
				}
			}
		}
	}
	private void logIfSet(String prefix, String relName, RelationInfo info) {
		if (info.otherSide != null) {
			log.log("%s: rule %s -> %s", prefix, relName, info.otherSide.fieldName);
		}
	}

	private RelationInfo findOtherSideNamed(DRule rrSrc, String relationName, DStructType farType, DStructType nearType, List<DeliaError> allErrors) {
		List<RelationInfo> nameRelL = new ArrayList<>();
		List<RelationInfo> relL = new ArrayList<>();
		for(DRule rule: farType.getRawRules()) {
			if (rule instanceof RelationOneRule) {
				RelationOneRule rr = (RelationOneRule) rule;
				if (rr.getRelationName().equals(relationName)) {
					nameRelL.add(rr.relInfo);
				} else {
					//otherwise find by field type 
					if (DRuleHelper.typesAreEqual(rr.relInfo.farType, nearType)) {
						relL.add(rr.relInfo);
					}
				}

			} else if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
				if (rr.getRelationName().equals(relationName)) {
					nameRelL.add(rr.relInfo);
				} else  {
					//otherwise find by field type 
					if (DRuleHelper.typesAreEqual(rr.relInfo.farType, nearType)) {
						relL.add(rr.relInfo);
					}
				}
			}
		}
		
		if (!nameRelL.isEmpty()) {
			if (nameRelL.size() > 1) {
				String s = nameRelL.get(0).relationName;
				String msg = String.format("Relation name '%s' used more than once (%d)", s, nameRelL.size());
				DeliaError err = new DeliaError("relation-names-must-be-unique", msg);
				allErrors.add(err);
			}
			return nameRelL.get(0);
		}
		
		if (relL.size() > 1) {
			String s = relL.get(0).relationName;
			String msg = String.format("ambigious relation '%s' could point to %d fields. Perhaps you need to use a named relation?", s, relL.size());
			DeliaError err = new DeliaError("ambiguous-relation", msg);
			allErrors.add(err);
		}
		return relL.isEmpty() ? null : relL.get(0);
	}
	
	
	private void step2HookupNamedOtherSide(List<DStructType> structsL, List<DeliaError> allErrors) {
		for(DStructType structType: structsL) {
			for(DRule rule: structType.getRawRules()) {
				if (rule instanceof RelationOneRule) {
					RelationOneRule rr = (RelationOneRule) rule;
					RelationInfo info = rr.relInfo;
					if (info.otherSide != null) {
						RelationInfo otherSide = info.otherSide;
						if (otherSide.otherSide == null) {
							otherSide.otherSide = info;
						} else if (otherSide.otherSide == info) {
						} else {
							String msg = String.format("Relation name '%s' already assigned", otherSide.relationName);
							DeliaError err = new DeliaError("relation-already-assigned", msg);
							allErrors.add(err);
						}
					}
				} else if (rule instanceof RelationManyRule) {
					RelationManyRule rr = (RelationManyRule) rule;
					RelationInfo info = rr.relInfo;
					if (info.otherSide != null) {
						RelationInfo otherSide = info.otherSide;
						if (otherSide.otherSide == null) {
							otherSide.otherSide = info;
						} else if (otherSide.otherSide == info) {
						} else {
							String msg = String.format("Relation name '%s' already assigned", otherSide.relationName);
							DeliaError err = new DeliaError("relation-already-assigned", msg);
							allErrors.add(err);
						}
					}
				}
			}
		}
	}
	private void step3HookupUnNamedRelations(List<DStructType> structsL, List<DeliaError> allErrors) {
		for(DStructType structType: structsL) {
			for(DRule rule: structType.getRawRules()) {
				if (rule instanceof RelationOneRule) {
					RelationOneRule rr = (RelationOneRule) rule;
					RelationInfo info = rr.relInfo;
					if (info.otherSide == null) {
						info.otherSide = findOtherSideUnNamed(rr, rr.getRelationName(), info.farType, info.nearType, allErrors);
						logIfSet("step3", rr.getRelationName(), info);
					}
				} else if (rule instanceof RelationManyRule) {
					RelationManyRule rr = (RelationManyRule) rule;
					RelationInfo info = rr.relInfo;
					if (info.otherSide == null) {
						info.otherSide = findOtherSideUnNamed(rr, rr.getRelationName(), info.farType, info.nearType, allErrors);
						logIfSet("step3", rr.getRelationName(), info);
					}
				}
			}
		}
	}

	private RelationInfo findOtherSideUnNamed(DRule rrSrc, String relationName, DStructType farType, DStructType nearType, List<DeliaError> allErrors) {
		List<RelationInfo> relL = new ArrayList<>();
		for(DRule rule: farType.getRawRules()) {
			if (rule instanceof RelationOneRule) {
				RelationOneRule rr = (RelationOneRule) rule;
				if (!rr.nameIsExplicit && rr.relInfo != null && DRuleHelper.typesAreEqual(rr.relInfo.farType, nearType)) {
					relL.add(rr.relInfo);
				}
			} else if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
				if (!rr.nameIsExplicit && rr.relInfo != null && DRuleHelper.typesAreEqual(rr.relInfo.farType, nearType)) {
					relL.add(rr.relInfo);
				}
			}
		}
		
		if (relL.size() > 1) {
			String s = relL.get(0).relationName;
			String msg = String.format("ambigious relation '%s' could point to %d fields. Perhaps you need to use a named relation?", s, relL.size());
			DeliaError err = new DeliaError("ambiguous-relation", msg);
			allErrors.add(err);
		}
		return relL.isEmpty() ? null : relL.get(0);
	}


	
	
	///////////////////////////////////////////////////////////
	private void setOtherSide(DTypeRegistry registry, List<DeliaError> allErrors) {
		for(String typeName: registry.getAll()) {
			DType dtype = registry.getType(typeName);
			if (! dtype.isStructShape()) {
				continue;
			}
			DStructType structType = (DStructType) dtype;
			
			for(DRule rule: structType.getRawRules()) {
				if (rule instanceof RelationOneRule) {
					RelationOneRule rr = (RelationOneRule) rule;
					RelationInfo info = rr.relInfo;
					info.otherSide = findOtherSide(rr, rr.getRelationName(), info.farType, info.nearType, allErrors);
					addErrorIfShouldBeHookedUp(info, rr.nameIsExplicit, allErrors);
				} else if (rule instanceof RelationManyRule) {
					RelationManyRule rr = (RelationManyRule) rule;
					RelationInfo info = rr.relInfo;
					info.otherSide = findOtherSide(rr, rr.getRelationName(), info.farType, info.nearType, allErrors);
					addErrorIfShouldBeHookedUp(info, rr.nameIsExplicit, allErrors);
				}
			}
		}
	}
	private void addErrorIfShouldBeHookedUp(RelationInfo info, boolean nameIsExplicit, List<DeliaError> allErrors) {
		if (info.otherSide == null && nameIsExplicit) {
			String s = info.relationName;
			String msg = String.format("named relation '%s' - cannot find other side of relation", s);
			DeliaError err = new DeliaError("named-relation-error", msg);
			allErrors.add(err);
		}
	}

	private RelationInfo findOtherSide(DRule rrSrc, String relationName, DStructType farType, DStructType nearType, List<DeliaError> allErrors) {
		List<RelationInfo> nameRelL = new ArrayList<>();
		List<RelationInfo> relL = new ArrayList<>();
		for(DRule rule: farType.getRawRules()) {
			if (rule instanceof RelationOneRule) {
				RelationOneRule rr = (RelationOneRule) rule;
//				if (rr.relInfo.otherSide != null) {
//					continue;
//				}
				
				if (rr.getRelationName().equals(relationName)) {
					nameRelL.add(rr.relInfo);
				} else {
					//otherwise find by field type 
					if (DRuleHelper.typesAreEqual(rr.relInfo.farType, nearType)) {
						relL.add(rr.relInfo);
					}
				}

			} else if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
//				if (rr.relInfo.otherSide != null) {
//					continue;
//				}
				
				if (rr.getRelationName().equals(relationName)) {
					nameRelL.add(rr.relInfo);
				} else  {
					//otherwise find by field type 
					if (DRuleHelper.typesAreEqual(rr.relInfo.farType, nearType)) {
						relL.add(rr.relInfo);
					}
				}
			}
		}
		
		if (!nameRelL.isEmpty()) {
			if (nameRelL.size() > 1) {
				String s = nameRelL.get(0).relationName;
				String msg = String.format("Relation name '%s' used more than once (%d)", s, nameRelL.size());
				DeliaError err = new DeliaError("relation-names-must-be-unique", msg);
				allErrors.add(err);
			}
			return nameRelL.get(0);
		}
		
		if (relL.size() > 1) {
			String s = relL.get(0).relationName;
			String msg = String.format("ambigious relation '%s' could point to %d fields. Perhaps you need to use a named relation?", s, relL.size());
			DeliaError err = new DeliaError("ambiguous-relation", msg);
			allErrors.add(err);
		}
		return relL.isEmpty() ? null : relL.get(0);
	}

	private void setParentFlagsIfNeeded(DTypeRegistry registry) {
		for(String typeName: registry.getAll()) {
			DType dtype = registry.getType(typeName);
			if (! dtype.isStructShape()) {
				continue;
			}
			DStructType structType = (DStructType) dtype;
			
			for(DRule rule: structType.getRawRules()) {
				if (rule instanceof RelationOneRule) {
					RelationOneRule rr = (RelationOneRule) rule;
					RelationInfo info = rr.relInfo;
					boolean isOptional = info.nearType.fieldIsOptional(info.fieldName);

					//if near is optional and far side is mandatory then near is parent
					if (isOptional) {
						RelationInfo farSideInfo = DRuleHelper.findOtherSideOne(info.farType, info.nearType);
						if (farSideInfo != null) {
							if (!farSideInfo.nearType.fieldIsOptional(farSideInfo.fieldName)) {
								rr.forceParentFlag(true);
								info.isParent = true;
							}
						}
					}
				} else if (rule instanceof RelationManyRule) {
					RelationManyRule rr = (RelationManyRule) rule;
					RelationInfo info = rr.relInfo;
					
//					boolean b = isOtherSideOne(info.farType, structType);
					boolean b = isOtherSideOne(info);
					info.isParent = b;
				}
			}
		}
	}
	
	private boolean isOtherSideOne(RelationInfo info) {
		return DRuleHelper.xfindOtherSideOne(info) != null;
	}
	private boolean isOtherSideMany(RelationInfo info) {
		return DRuleHelper.xfindOtherSideMany(info) != null;
	}
	private boolean isOtherSideMany(DType otherSide, TypePair otherRelPair) {
		return DRuleHelper.isOtherSideMany(otherSide, otherRelPair);
	}
	private void checkForOtherSideDuplicates(DTypeRegistry registry, List<DeliaError> allErrors) {
		Map<RelationInfo,String> duplicateMap = new HashMap<>();
		
		for(String typeName: registry.getAll()) {
			DType dtype = registry.getType(typeName);
			if (! dtype.isStructShape()) {
				continue;
			}
			DStructType structType = (DStructType) dtype;
			
			for(DRule rule: structType.getRawRules()) {
				if (rule instanceof RelationOneRule) {
					RelationOneRule rr = (RelationOneRule) rule;
					RelationInfo info = rr.relInfo;
					ensureNotAlreadyUsed(duplicateMap, info.otherSide, allErrors);
				} else if (rule instanceof RelationManyRule) {
					RelationManyRule rr = (RelationManyRule) rule;
					RelationInfo info = rr.relInfo;
					ensureNotAlreadyUsed(duplicateMap, info.otherSide, allErrors);
				}
			}
		}
	}

	
	private void ensureNotAlreadyUsed(Map<RelationInfo, String> duplicateMap, RelationInfo otherSide, List<DeliaError> allErrors) {
		if (otherSide == null) {
			return;
		}
		if (duplicateMap.containsKey(otherSide)) {
			String s = otherSide.relationName;
			String msg = String.format("named relation '%s' - already assigned to another relation", s);
			DeliaError err = new DeliaError("relation-already-assigned", msg);
			allErrors.add(err);
			
		}
		duplicateMap.put(otherSide, "");
	}

	
	
}
