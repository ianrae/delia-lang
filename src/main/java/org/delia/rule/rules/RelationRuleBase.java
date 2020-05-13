package org.delia.rule.rules;

import org.delia.relation.RelationInfo;
import org.delia.rule.DRuleBase;
import org.delia.rule.RuleGuard;
import org.delia.rule.RuleOperand;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;

public abstract class RelationRuleBase extends DRuleBase {
	protected RuleOperand oper1;
	protected DStructType owningType;
	protected DTypeRegistry registry;
	public RelationInfo relInfo;
	private String relationName; //either user-defined or delia assigns a name
	public boolean nameIsExplicit; //delia source had a name for the relation
	private Integer datId; //many-to-many table id

	public RelationRuleBase(String ruleName, RuleGuard guard, RuleOperand oper1, 
			DStructType owningType, DTypeRegistry registry, String relationName) {
		super(ruleName, guard);
		this.oper1 = oper1;
		this.owningType = owningType;
		this.registry = registry;
		this.relationName = relationName;
	}
	
	@Override
	public boolean dependsOn(String fieldName) {
		return oper1.dependsOn(fieldName);
	}
	@Override
	public String getSubject() {
		return oper1.getSubject();
	}
	
	public String getRelationName() {
		return relationName;
	}
	public Integer getDatId() {
		return datId;
	}
	public void forceDatId(Integer datId) {
		this.datId = datId;
	}
}