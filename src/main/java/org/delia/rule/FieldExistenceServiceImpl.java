package org.delia.rule;

import org.delia.error.ErrorTracker;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;

public class FieldExistenceServiceImpl implements FieldExistenceService {

	private DTypeRegistry registry;
	private DType dtype;
	
	public FieldExistenceServiceImpl(DTypeRegistry registry, DType dtype) {
		this.registry = registry;
		this.dtype = dtype;
	}
	@Override
	public boolean existField(String fieldName) {
		DStructType structType = (DStructType) dtype;
		return structType.hasField(fieldName);
	}
	@Override
	public void checkRuleOperand(String ruleName, RuleOperand oper, ErrorTracker et) {
		if (oper instanceof StructDValueRuleOperand) {
			StructDValueRuleOperand sro = (StructDValueRuleOperand) oper;
			checkFieldName(ruleName, sro.getSubject(), et);
		}
	}
	@Override
	public void checkFieldName(String ruleName, String fieldName, ErrorTracker et) {
		if (! existField(fieldName)) {
			String msg = String.format("Rule %s: unknown field '%s'", ruleName, fieldName);
			et.add("rule-op-not-found", msg);
		}
	}

}
