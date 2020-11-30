package org.delia.sort.topo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;


public class DeliaTypeSorter {
	
	public List<String> topoSort(DTypeRegistry registry) {
		
		List<DStructType> typeL = getStructs(registry);
		DirectedGraph<String> graph = new DirectedGraph<>();

		HashSet<DType> extset = new HashSet<>();
		for(DType dtype: typeL) {
			DStructType structType = (DStructType) dtype;
			graph.addNode(structType.getName());
			extset.add(structType);
			
			for(DRule rule: structType.getRawRules()) {
				if (rule instanceof RelationOneRule) {
					RelationOneRule rr = (RelationOneRule) rule;
					RelationInfo info = rr.relInfo;
					if (info.isParent) {
						continue;
					}
					TypePair pair = findMatchingPair(structType, info.fieldName);
					
					if (! extset.contains(pair.type)) {
						graph.addNode(pair.type.getName());
						extset.add(pair.type);
					}
					
					if (structType == pair.type) {
						//System.out.println(String.format("%s self-join", structType.getName()));
						if (info.otherSide != null && info.relationName.equals(info.otherSide.relationName)) {
							System.out.println(String.format("skipping self-join %s on %s", structType.getName(), pair.type.getName()));
							continue;
						}
					}
					
					System.out.println(String.format("%s depends on %s", structType.getName(), pair.type.getName()));
					graph.addEdge(structType.getName(), pair.type.getName()); //structType depends on pair type
				} else if (rule instanceof RelationManyRule) {
					RelationManyRule rr = (RelationManyRule) rule;
					RelationInfo info = rr.relInfo;
					if (info.isParent) {
						continue;
					}
					if (info.cardinality.equals(RelationCardinality.MANY_TO_MANY)) {
						continue;
					}
					TypePair pair = findMatchingPair(structType, info.fieldName);
					
					if (! extset.contains(pair.type)) {
						graph.addNode(pair.type.getName());
						extset.add(pair.type);
					}
					
					if (structType == pair.type) {
						//System.out.println(String.format("%s self-join", structType.getName()));
						if (info.otherSide != null && info.relationName.equals(info.otherSide.relationName)) {
							System.out.println(String.format("skipping self-join %s on %s", structType.getName(), pair.type.getName()));
							continue;
						}
					}
					
					System.out.println(String.format("%s depends on %s", structType.getName(), pair.type.getName()));
					graph.addEdge(structType.getName(), pair.type.getName()); //structType depends on pair type
					
				}
			}
		}
		List<String> sortedL = TopologicalSort.sort(graph);
		Collections.reverse(sortedL);
		return sortedL;
	}
	
	private TypePair findMatchingPair(DStructType structType, String fieldName) {
		return DRuleHelper.findMatchingStructPair(structType, fieldName);
	}

//	protected RelationOneRule findOneRule(String typeName, DTypeRegistry registry) {
//		return DRuleHelper.findOneRule(typeName, registry);
//	}


	private List<DStructType> getStructs(DTypeRegistry ds) {
		List<DStructType> list = new ArrayList<>();
		for(String typeName: ds.getAll()) {
			DType dtype = ds.getType(typeName);
			if (dtype.isStructShape()) {
				list.add((DStructType) dtype);
			}
		}
		return list;
	}
}