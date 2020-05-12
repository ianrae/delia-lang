package org.delia.runner;

import java.util.ArrayList;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
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
		buildRelInfos(registry);
		setOtherSide(registry, allErrors);
		setParentFlagsIfNeeded(registry);
		
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

	private void buildRelInfos(DTypeRegistry registry) {
		for(String typeName: registry.getAll()) {
			DType dtype = registry.getType(typeName);
			if (! dtype.isStructShape()) {
				continue;
			}
			DStructType structType = (DStructType) dtype;
			
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
				} else if (rule instanceof RelationManyRule) {
					RelationManyRule rr = (RelationManyRule) rule;
					RelationInfo info = rr.relInfo;
					info.otherSide = findOtherSide(rr, rr.getRelationName(), info.farType, info.nearType, allErrors);
				}
			}
		}
	}
	
	private RelationInfo findOtherSide(DRule rrSrc, String relationName, DStructType farType, DStructType nearType, List<DeliaError> allErrors) {
		List<RelationInfo> nameRelL = new ArrayList<>();
		List<RelationInfo> relL = new ArrayList<>();
		for(DRule rule: farType.getRawRules()) {
			if (rule instanceof RelationOneRule) {
				RelationOneRule rr = (RelationOneRule) rule;
				if (rr.getRelationName().equals(relationName)) {
					nameRelL.add(rr.relInfo);
				}

				//otherwise find by field type 
				if (DRuleHelper.typesAreEqual(rr.relInfo.farType, nearType)) {
					relL.add(rr.relInfo);
				}
			} else if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
				if (rr.getRelationName().equals(relationName)) {
					nameRelL.add(rr.relInfo);
				}
					
				//otherwise find by field type 
				if (DRuleHelper.typesAreEqual(rr.relInfo.farType, nearType)) {
					relL.add(rr.relInfo);
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
		return relL.get(0);
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
					
					boolean b = isOtherSideOne(info.farType, structType);
					info.isParent = b;
				}
			}
		}
	}
	
	private boolean isOtherSideOne(DType otherSide, DStructType structType) {
		return DRuleHelper.isOtherSideOne(otherSide, structType);
	}
	private boolean isOtherSideMany(DType otherSide, TypePair otherRelPair) {
		return DRuleHelper.isOtherSideMany(otherSide, otherRelPair);
	}
	
}
