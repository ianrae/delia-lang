package org.delia.runner;

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
		setParentFlagsIfNeeded(registry);
		
		//thenvalidate types can be put into a correct dependency order (i.e. no cycles)
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
						}
					}
				}
			}
		}
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
//					info.isParent = rr.isParent();
				} else if (rule instanceof RelationManyRule) {
					RelationManyRule rr = (RelationManyRule) rule;
					RelationInfo info = rr.relInfo;
					
//					TypePair pair = findMatchingPair(info.farType, info.fieldName);
//					TypePair otherSidePair = new TypePair(info.fieldName, structType);
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
	
//	type Customer struct {id int unique, relation addr Address optional many } end
//	type Address struct {id int unique, relation cust Customer optional one } end	
}
