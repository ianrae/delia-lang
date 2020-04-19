package org.delia.rule.fns;

import java.util.Date;

import org.delia.compiler.ast.Exp;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.rule.DRuleBase;
import org.delia.rule.DRuleContext;
import org.delia.rule.RuleGuard;
import org.delia.type.DValue;

public class DateMakeFnRule extends DRuleBase {

	private Exp arg;
	private FactoryService factorySvc;
	private DateFormatService fmtSvc;

	public DateMakeFnRule(FactoryService factorySvc, RuleGuard guard, Exp arg) {
		super("date", guard);
		this.factorySvc = factorySvc;
		this.fmtSvc = factorySvc.getDateFormatService();
		this.arg = arg;
	}
	@Override
	protected boolean onValidate(DValue dval, DRuleContext ctx) {
		return false; //not allowed!
	}
	@Override
	public Object exec(DValue dval, DRuleContext ctx) {
		Date dt = fmtSvc.parse(arg.strValue());
		return dt;
	}
	@Override
	public boolean dependsOn(String fieldName) {
		// TODO fix this. what are we getting the length of?
		return false;
	}
	@Override
	public String getSubject() {
		return "";
	}
}