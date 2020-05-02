package org.delia.tlang.statement;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.tlang.runner.TLangContext;
import org.delia.tlang.runner.TLangResult;
import org.delia.type.DValue;

public class VariableStatement extends TLangStatementBase {
	private String varName;
	public VariableStatement(String varName) {
		super("var");
		this.varName = varName;
	}
	@Override
	public void execute(DValue value, TLangResult result, TLangContext ctx) {
		List<DValue> list = ctx.varEvaluator.lookupVar(varName);
		if (CollectionUtils.isEmpty(list)) {
			result.val = null;
		} else {
			result.val = list.get(0); //first only
		}
	}
	public String getVarName() {
		return varName;
	}

}