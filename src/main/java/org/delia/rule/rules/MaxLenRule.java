package org.delia.rule.rules;

import org.delia.error.ErrorTracker;
import org.delia.rule.*;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.util.StringUtil;

public class MaxLenRule extends DRuleBase {
    private RuleOperand oper1;
    private int maxlen;

    public MaxLenRule(RuleGuard guard, RuleOperand oper1, int maxlen) {
        super("maxlen", guard);
        this.oper1 = oper1;
        this.maxlen = maxlen;
    }

    @Override
    public String renderAsDelia(RuleGeneratorContext ctx) {
        return String.format("%smaxlen(%d)", ctx.getOperAsDelia(oper1), maxlen);
    }


    @Override
    public void performCompilerPass4Checks(DType dtype, FieldExistenceService fieldExistSvc, ErrorTracker et) {
        fieldExistSvc.checkRuleOperand(getName(), oper1, et);
    }

    @Override
    protected boolean onValidate(DValue dval, DRuleContext ctx) {
        String s = oper1.asString(dval);

        if (s.length() > maxlen) {
            String s1 = StringUtil.atMostChars(s, 80);
            String msg = String.format("string longer than %d: '%s'", maxlen, s1);
            ctx.addError(this, msg, oper1);
            return false;
        }
        return true;
    }

    @Override
    public boolean dependsOn(String fieldName) {
        return oper1.dependsOn(fieldName);
    }

    @Override
    public String getSubject() {
        return oper1.getSubject();
    }
}