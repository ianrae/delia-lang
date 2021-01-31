package org.delia.rule.rules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.delia.compiler.ast.QueryExp;
import org.delia.db.DBHelper;
import org.delia.db.QueryBuilderService;
import org.delia.hld.HLDSimpleQueryService;
import org.delia.rule.DRuleBase;
import org.delia.rule.DRuleContext;
import org.delia.rule.RuleGuard;
import org.delia.rule.RuleOperand;
import org.delia.runner.QueryResponse;
import org.delia.type.DValue;
import org.delia.util.StringUtil;
import org.delia.zdb.DBExecutor;

public class UniqueFieldsRule extends DRuleBase {
	private List<RuleOperand> operL;

	public UniqueFieldsRule(RuleGuard guard, List<RuleOperand> operL) {
		super("uniqueFields", guard);
		this.operL = operL;
	}
	@Override
	protected boolean onValidate(DValue dval, DRuleContext ctx) {
		if (ctx.getDBCapabilities().supportsUniqueConstraint()) {
			return true; //db will do this validation
		}
		
		if (operL.isEmpty()) {
			return true;
		}
		
		Map<String,String> map = new HashMap<>();
		if (ctx.isInsertFlag()) {
			String key = buildKey(dval);
			map.put(key, "");
		}
		
		//MEM
		DBExecutor dbexecutor = ctx.getDbInterface().createExecutor();
		dbexecutor.init1(ctx.getRegistry());
		try {
			QueryResponse qresp = doQueryAll(dval, dbexecutor, ctx);
			
			ctx.getLog().log("xxxxxxzz " + qresp.dvalList.size());
			for(DValue inner: qresp.dvalList) {
				String key = buildKey(inner);
				if (map.containsKey(key)) {
					List<String> nameL = operL.stream().map(x -> x.getSubject()).collect(Collectors.toList());
					String msg = String.format("fields (%s) are not unique", StringUtil.flatten(nameL));
					ctx.addError(this, msg, operL.get(0));
					return false;
				}
				map.put(key, "");
			}
			ctx.getLog().log("xxxxxxszz " + map.size());
			
		} finally {
			if (dbexecutor != null) {
				try {
					dbexecutor.close();
				} catch (Exception e) {
					DBHelper.handleCloseFailure(e);
				}
			}
		}
		return true;
	}
	
	private String buildKey(DValue dval) {
		//TODO: improve this algorithm. need proper way to make key from many dvalue fields
		StringJoiner joiner = new StringJoiner(",");
		for(RuleOperand oper: operL) {
			String fieldName = oper.getSubject();
			DValue inner = dval.asStruct().getField(fieldName);
			if (inner == null) {
				joiner.add("___NULL___");
			} else {
				joiner.add(inner.asString());
			}
		}
		
		return joiner.toString();
	}
	private QueryResponse doQueryAll(DValue dval, DBExecutor dbexecutor, DRuleContext ctx) {
		QueryBuilderService queryBuilder = ctx.getFactorySvc().getQueryBuilderService();
		QueryExp queryExp = queryBuilder.createAllRowsQuery(dval.getType().getName());
		HLDSimpleQueryService querySvc = ctx.getFactorySvc().createHLDSimpleQueryService(ctx.getDbInterface(), ctx.getRegistry());
		
		QueryResponse qresp = querySvc.execQuery(queryExp, dbexecutor);
		return qresp;
	}
		
	@Override
	public boolean dependsOn(String fieldName) {
		for(RuleOperand oper: operL) {
			if (oper.dependsOn(fieldName)) {
				return true;
			}
		}
		return false;
	}
	@Override
	public String getSubject() {
		if (operL.isEmpty()) return null;
		return operL.get(0).getSubject(); //TODO: is this ok
	}
}