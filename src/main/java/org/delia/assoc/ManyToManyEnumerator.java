package org.delia.assoc;

import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;

public class ManyToManyEnumerator {
	public void visitTypes(DTypeRegistry registry, ManyToManyVisitor visitor) {
		for(DType dtype: registry.getOrderedList()) {
			if (! dtype.isStructShape()) {
				continue;
			}
			DStructType structType = (DStructType) dtype;
			for(DRule rule: structType.getRawRules()) {
				if (rule instanceof RelationManyRule) {
					RelationManyRule rr = (RelationManyRule) rule;
					if (rr.relInfo.isManyToMany()) {
						visitor.visit(structType, rr);
					}
				}
			}
		}
	}
}