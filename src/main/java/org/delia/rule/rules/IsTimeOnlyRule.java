package org.delia.rule.rules;

import org.delia.error.ErrorTracker;
import org.delia.rule.*;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

import static java.util.Objects.isNull;

public class IsTimeOnlyRule extends ShapeSpecificRuleBase {
    private RuleOperand oper1;

    public IsTimeOnlyRule(RuleGuard guard, RuleOperand oper1) {
        super("isTimeOnly", guard);
        this.oper1 = oper1;
    }
    @Override
    public String renderAsDelia(RuleGeneratorContext ctx) {
        return String.format("%sisTimeOnly()", ctx.getOperAsDelia(oper1));
    }

    @Override
    public void performCompilerPass4Checks(DType dtype, FieldExistenceService fieldExistSvc, ErrorTracker et) {
        fieldExistSvc.checkRuleOperand(getName(), oper1, et);
        String errId = String.format("%s-not-allowed", getName());

        DType typeToCheck = foo(dtype, oper1.getSubject(), errId, et);
        if (isNull(typeToCheck)) {
            return;
        }

        switch (typeToCheck.getShape()) {
            case DATE:
                pass4DateCheck(et);
                break;
            default:
                et.add(errId, String.format("%s rule not supported for type '%s' with shape '%s'", getName(), typeToCheck.getName(), typeToCheck.getShape().name()));
                break;
        }

    }

    private void pass4DateCheck(ErrorTracker et) {
    }


    @Override
    protected boolean onValidate(DValue dval, DRuleContext ctx) {
        String fieldName = oper1.getSubject();
        DValue inner = dval.getType().isStructShape() ? dval.asStruct().getField(fieldName) : dval;

        switch (inner.getType().getShape()) {
            case DATE:
                return validateDate(dval, ctx);
            default:
                DeliaExceptionHelper.throwError("rule-wrong-field-type", String.format("%s only supported on date types", getName()));
                break;
        }

        return true;
    }

    //nothing to validate. isDateOnly is only used to determine sql column type and when parsing delia source values
    private boolean validateDate(DValue dval, DRuleContext ctx) {
        return true; //TODO what do do here?
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