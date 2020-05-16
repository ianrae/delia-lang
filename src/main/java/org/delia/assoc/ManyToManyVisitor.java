package org.delia.assoc;

import org.delia.rule.rules.RelationRuleBase;
import org.delia.type.DStructType;

public interface ManyToManyVisitor {
	void visit(DStructType structType, RelationRuleBase rr);
}