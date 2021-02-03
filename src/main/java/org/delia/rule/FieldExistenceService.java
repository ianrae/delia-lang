package org.delia.rule;

import org.delia.error.ErrorTracker;

public interface FieldExistenceService {

	boolean existField(String fieldName);
	void checkRuleOperand(String ruleName, RuleOperand oper, ErrorTracker et);
	void checkFieldName(String ruleName, String fieldName, ErrorTracker et);
}
