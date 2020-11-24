package org.delia.rule.fns;

import java.time.ZonedDateTime;

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
		ZonedDateTime zdt = fmtSvc.parseDateTime(arg.strValue());
		return zdt;
	}
	@Override
	public boolean dependsOn(String fieldName) {
		return false;
	}
	@Override
	public String getSubject() {
		return "";
	}
}