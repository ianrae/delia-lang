package org.delia.sort.topo;

import org.delia.log.DeliaLog;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.type.*;
import org.delia.util.DRuleHelper;

import java.util.*;


public class DeliaTypeSorter {
    //in cases where type A has fk to type B without using a relation
    private Map<DTypeName, List<DTypeName>> customRefMap = new HashMap<>();

    public void addCustomDependency(DTypeName type1, DTypeName dependency) {
        if (!customRefMap.containsKey(type1)) {
            List<DTypeName> list = new ArrayList<>();
            list.add(dependency);
            customRefMap.put(type1, list);
        } else {
            List<DTypeName> list = customRefMap.get(type1);
            list.add(dependency);
        }
    }


    public List<DTypeName> topoSort(DTypeRegistry registry, DeliaLog log) {

        List<DStructType> typeL = getStructs(registry);
        DirectedGraph<DTypeName> graph = new DirectedGraph<>();

        HashSet<DType> extset = new HashSet<>();
        for (DType dtype : typeL) {
            DStructType structType = (DStructType) dtype;
            graph.addNode(structType.getTypeName());
            extset.add(structType);

            if (customRefMap.containsKey(structType.getTypeName())) {
                for (DTypeName dependency : customRefMap.get(structType.getTypeName())) {
					if (!extset.contains(structType)) {
						graph.addNode(structType.getTypeName());
						extset.add(structType);
					}
					log.logDebug("%s depends on %s", structType.getTypeName(), dependency);
					graph.addEdge(structType.getTypeName(), dependency);
                }
            }

            for (DRule rule : structType.getRawRules()) {
                if (rule instanceof RelationOneRule) {
                    RelationOneRule rr = (RelationOneRule) rule;
                    RelationInfo info = rr.relInfo;
                    if (info.isParent) {
                        continue;
                    }
                    TypePair pair = findMatchingPair(structType, info.fieldName);

                    if (!extset.contains(pair.type)) {
                        graph.addNode(pair.type.getTypeName());
                        extset.add(pair.type);
                    }

                    if (structType == pair.type) {
                        log.logDebug("%s self-join", structType.getName());
                        if (info.otherSide != null && info.relationName.equals(info.otherSide.relationName)) {
                            log.logDebug("skipping self-join %s on %s", structType.getName(), pair.type.getName());
                            continue;
                        }
                    }

                    log.logDebug("%s depends on %s", structType.getName(), pair.type.getName());
                    graph.addEdge(structType.getTypeName(), pair.type.getTypeName()); //structType depends on pair type
                } else if (rule instanceof RelationManyRule) {
                    RelationManyRule rr = (RelationManyRule) rule;
                    RelationInfo info = rr.relInfo;
                    if (info.isParent) {
                        continue;
                    }
                    if (info.cardinality.equals(RelationCardinality.MANY_TO_MANY)) {
                        boolean b1 = info.nearType.fieldIsOptional(info.fieldName);
                        boolean b2 = false;
                        if (info.otherSide != null && !info.otherSide.isParent) {
                            b2 = info.otherSide.nearType.fieldIsOptional(info.otherSide.fieldName);
                        }
                        if (!b1 && b2) {
                            //if b1 is mandatory then create an edge
                        } else {
                            continue;
                        }
                    }
                    TypePair pair = findMatchingPair(structType, info.fieldName);

                    if (!extset.contains(pair.type)) {
                        graph.addNode(pair.type.getTypeName());
                        extset.add(pair.type);
                    }

                    if (structType == pair.type) {
                        log.logDebug("%s self-join", structType.getName());
                        if (info.otherSide != null && info.relationName.equals(info.otherSide.relationName)) {
                            log.logDebug("skipping self-join %s on %s", structType.getName(), pair.type.getName());
                            continue;
                        }
                    }

                    log.logDebug("%s depends on %s", structType.getName(), pair.type.getName());
                    graph.addEdge(structType.getTypeName(), pair.type.getTypeName()); //structType depends on pair type
                }
            }
        }

        List<DTypeName> sortedL = TopologicalSort.sort(graph);
        //sortedL is like order of module dependency with app at top, and platform at bottom
        //reverse the list so that lowest-level types defined first, then types that depend on them
//		if (isDifferent(typeL, sortedL)) {
        Collections.reverse(sortedL);
//		}
        return sortedL;
    }

//	private boolean isDifferent(List<DStructType> typeL, List<DTypeName> sortedL) {
//		int index = 0;
//		for(DType dtype: typeL) {
//			DTypeName t1 = dtype.getTypeName();;
//			DTypeName t2 = sortedL.get(index++);
//			if (!t1.equals(t2)) {
//				return true;
//			}
//		}
//		return false;
//	}

    private TypePair findMatchingPair(DStructType structType, String fieldName) {
        return DRuleHelper.findMatchingStructPair(structType, fieldName);
    }

//	protected RelationOneRule findOneRule(String typeName, DTypeRegistry registry) {
//		return DRuleHelper.findOneRule(typeName, registry);
//	}


    private List<DStructType> getStructs(DTypeRegistry ds) {
        List<DStructType> list = new ArrayList<>();
        for (DType dtype : ds.getOrderedList()) {
            if (dtype.isStructShape()) {
                list.add((DStructType) dtype);
            }
        }
        return list;
    }
}