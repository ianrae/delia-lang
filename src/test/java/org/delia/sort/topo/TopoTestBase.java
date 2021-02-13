package org.delia.sort.topo;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.scope.scopetest.relation.NewRelationTestBase;
import org.delia.sort.topo.DeliaTypeSorter;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.util.StringUtil;

public class TopoTestBase extends NewRelationTestBase {
	
	protected void chkRelInfo(RelationInfo info, RelationCardinality card, String typeNear, String fieldName, String typeFar, boolean bParent, boolean bOneWay) {
		assertEquals(card, info.cardinality);
		assertEquals(typeFar, info.farType.getName());
		assertEquals(fieldName, info.fieldName);
		assertEquals(bOneWay, info.isOneWay);
		assertEquals(bParent, info.isParent);
		assertEquals(typeNear, info.nearType.getName());
	}
	
	protected RelationOneRule findOneRule(String typeName) {
		DStructType dtype = (DStructType) this.sess.getExecutionContext().registry.getType(typeName);
		for(DRule rule: dtype.getRawRules()) {
			if (rule instanceof RelationOneRule) {
				return (RelationOneRule) rule;
			}
		}
		return null;
	}
	protected RelationManyRule findManyRule(String typeName) {
		DStructType dtype = (DStructType) this.sess.getExecutionContext().registry.getType(typeName);
		for(DRule rule: dtype.getRawRules()) {
			if (rule instanceof RelationManyRule) {
				return (RelationManyRule) rule;
			}
		}
		return null;
	}
	
	protected void chkSorting(String expected) {
		DTypeRegistry reg = this.sess.getExecutionContext().registry;
		assertEquals(DTypeRegistry.NUM_BUILTIN_TYPES + 2, reg.size());
		assertEquals(true, reg.existsType("Customer"));
		assertEquals(true, reg.existsType("Address"));
		
		DeliaTypeSorter sorter = new DeliaTypeSorter();
		List<String> list = sorter.topoSort(reg);
		String s = StringUtil.flatten(list);
		assertEquals(expected, s);
	}
}
