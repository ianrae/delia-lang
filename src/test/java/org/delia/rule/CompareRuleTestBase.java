package org.delia.rule;

import org.delia.scope.scopetest.relation.DeliaClientTestBase;

public class CompareRuleTestBase extends DeliaClientTestBase {
	
	
	// --
	protected int nextIdNum = 1;
	protected String ruleText;

	protected void chkPass(String fieldType, String fieldStr) {
		String src = buildSrc(fieldType, fieldStr);
		execTypeStatement(src);
	}
	protected void chkFail(String fieldType, String fieldStr) {
		String src = buildSrc(fieldType, fieldStr);
		execTypeStatementFail(src, "rule-compare");
	}

	protected String buildSrc(String fieldType, String fieldStr) {
		String src = String.format("type Customer struct {id int unique, wid %s } %s end", fieldType, ruleText);
		src += "\n";
		String idStr = String.format("%d", nextIdNum++);
		src += String.format("insert Customer {id: %s, %s }", idStr, fieldStr);
		return src;
	}


}
