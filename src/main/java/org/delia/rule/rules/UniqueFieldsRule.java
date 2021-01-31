package org.delia.rule.rules;

import java.util.List;

import org.delia.compiler.ast.Exp;
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
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
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
		
		//MEM
		//case 1. first field is pk
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
		RuleOperand first = operL.get(0);
		if (pkpair != null && first.getSubject().equals(pkpair.name)) {
			//pk is already unique by itself. so just need to validate other fields
			
			QueryBuilderService queryBuilder = ctx.getFactorySvc().getQueryBuilderService();
			QueryExp queryExp = queryBuilder.createAllRowsQuery(dval.getType().getName());
			HLDSimpleQueryService querySvc = ctx.getFactorySvc().createHLDSimpleQueryService(ctx.getDbInterface(), ctx.getRegistry());
			
			DBExecutor dbexecutor = ctx.getDbInterface().createExecutor();
			try {
				QueryResponse z = querySvc.execQuery(queryExp, dbexecutor);
			} finally {
				if (dbexecutor != null) {
					try {
						dbexecutor.close();
					} catch (Exception e) {
						DBHelper.handleCloseFailure(e);
					}
				}
			}
			
		}
		
		
		return true;
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